package com.elearning.platform.services;

import com.elearning.platform.config.VlearningProperties;
import com.elearning.platform.dto.AuthDtos.DesbloqueoRespuesta;
import com.elearning.platform.dto.AuthDtos.MensajeRespuesta;
import com.elearning.platform.entity.TokenDesbloqueo;
import com.elearning.platform.entity.Usuario;
import com.elearning.platform.enums.EstadoUsuario;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.RegistroAuditoriaRepository;
import com.elearning.platform.repository.TokenDesbloqueoRepository;
import com.elearning.platform.repository.UsuarioRepository;
import com.elearning.platform.util.Hashes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * RF-043: desbloqueo de la cuenta mediante un enlace de un solo uso enviado al correo cuando la
 * cuenta se bloquea por intentos fallidos (RF-002). Reutiliza el patrón de RecuperacionService.
 * Tras 3 bloqueos en 24 horas la cuenta queda bloqueada hasta que la desbloquee un administrador.
 */
@Service
@RequiredArgsConstructor
public class DesbloqueoService {

    /** RF-043: el enlace vence a los 15 minutos. */
    static final int MINUTOS_VIGENCIA = 15;
    /** RF-043: a partir del tercer bloqueo en 24 horas, desbloquea el administrador. */
    static final int BLOQUEOS_PARA_ADMINISTRADOR = 3;
    static final int HORAS_VENTANA_BLOQUEOS = 24;
    /** Segundos mínimos entre dos enlaces pedidos a mano (evita usar el endpoint para enviar spam). */
    static final int SEGUNDOS_ENTRE_ENLACES = 60;

    static final String ACCION_BLOQUEO = "BLOQUEO_TEMPORAL";

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final String MENSAJE_GENERICO =
            "Si tu cuenta está bloqueada por intentos fallidos, te enviamos un enlace para desbloquearla.";

    private final UsuarioRepository usuarios;
    private final TokenDesbloqueoRepository tokens;
    private final RegistroAuditoriaRepository registros;
    private final MailService correo;
    private final NotificacionService notificaciones;
    private final AuditoriaService auditoria;
    private final VlearningProperties props;

    @Value("${vlearning.desbloqueo.url-base:http://localhost:5173/desbloquear}")
    private String urlBase;

    /** RF-043: true si el número de bloqueos en las últimas 24 h exige al administrador. */
    static boolean requiereAdministrador(long bloqueosEn24Horas) {
        return bloqueosEn24Horas >= BLOQUEOS_PARA_ADMINISTRADOR;
    }

    /**
     * RF-043: true si el bloqueo solo lo puede levantar un administrador: bloqueo manual sin
     * vencimiento, o el bloqueo largo que se aplica a una cuenta en primer acceso.
     */
    public static boolean requiereAdministrador(Usuario u, LocalDateTime ahora) {
        if (u.getEstado() == EstadoUsuario.BLOQUEADO && u.getBloqueadoHasta() == null) return true;
        return u.getBloqueadoHasta() != null && u.getBloqueadoHasta().isAfter(ahora.plusDays(1));
    }

    /** RF-043: true si ya pasó la espera mínima desde el último enlace. */
    static boolean puedeEnviarOtro(LocalDateTime ultimoEnlace, LocalDateTime ahora) {
        return ultimoEnlace == null || !ultimoEnlace.isAfter(ahora.minusSeconds(SEGUNDOS_ENTRE_ENLACES));
    }

    /**
     * RF-043: se llama justo después de bloquear la cuenta por intentos fallidos (el bloqueo ya está
     * auditado). Envía el enlace o, si es el tercer bloqueo en 24 h, deja la cuenta bloqueada hasta
     * que intervenga un administrador. Devuelve el enlace solo en modo desarrollo.
     */
    @Transactional
    public Optional<String> alBloquear(Usuario u, String ip) {
        long bloqueos = registros.countByUsuarioIdAndAccionAndResultadoAndFechaAfter(u.getId(), ACCION_BLOQUEO,
                ResultadoAuditoria.DENEGADO, LocalDateTime.now().minusHours(HORAS_VENTANA_BLOQUEOS));
        if (requiereAdministrador(bloqueos)) {
            exigirAdministrador(u, ip);
            return Optional.empty();
        }
        String enlace = emitirEnlace(u, ip);
        notificaciones.notificar(u.getId(), "Tu cuenta fue bloqueada temporalmente",
                "Detectamos varios intentos fallidos de inicio de sesión. Te enviamos a tu correo un enlace para "
                        + "desbloquearla. Si no fuiste tú, cambia tu contraseña cuando entres.", true);
        return props.dev().exponerSecretos() ? Optional.of(enlace) : Optional.empty();
    }

    /** RF-043: pedir un enlace nuevo (si el anterior venció o se usó). Respuesta genérica siempre. */
    @Transactional
    public DesbloqueoRespuesta solicitar(String correoIngresado, String ip) {
        String correoNormalizado = correoIngresado.trim().toLowerCase();
        Usuario u = usuarios.findByCorreoInstitucionalIgnoreCase(correoNormalizado).orElse(null);
        if (u == null || !u.estaBloqueadoTemporalmente() || requiereAdministrador(u, LocalDateTime.now())) {
            auditoria.registrar(u == null ? null : u.getId(), "DESBLOQUEO_SOLICITADO", correoNormalizado,
                    ResultadoAuditoria.DENEGADO, ip);
            return new DesbloqueoRespuesta(MENSAJE_GENERICO, null);
        }
        LocalDateTime ultimo = tokens.findFirstByUsuarioIdOrderByFechaCreacionDesc(u.getId())
                .map(TokenDesbloqueo::getFechaCreacion).orElse(null);
        if (!puedeEnviarOtro(ultimo, LocalDateTime.now())) {
            throw new ApiException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "ENLACE_MUY_PRONTO",
                    "Ya te enviamos un enlace hace un momento. Revisa tu correo y espera un minuto para pedir otro.");
        }
        String enlace = emitirEnlace(u, ip);
        auditoria.registrar(u.getId(), "DESBLOQUEO_SOLICITADO", correoNormalizado, ResultadoAuditoria.PERMITIDO, ip);
        return new DesbloqueoRespuesta(MENSAJE_GENERICO, props.dev().exponerSecretos() ? enlace : null);
    }

    /** RF-043: canjea el enlace, desbloquea la cuenta y lo registra en auditoría. */
    @Transactional(noRollbackFor = ApiException.class)
    public MensajeRespuesta desbloquear(String token, String ip) {
        TokenDesbloqueo t = tokens.findByTokenHash(Hashes.sha256Hex(token.trim())).orElse(null);
        if (t == null || !t.vigente()) {
            auditoria.registrar(t == null ? null : t.getUsuarioId(), "DESBLOQUEO_ENLACE_INVALIDO",
                    "enlace vencido o usado", ResultadoAuditoria.DENEGADO, ip);
            throw ApiException.solicitudInvalida("ENLACE_INVALIDO",
                    "El enlace venció o ya se usó. Pide uno nuevo con tu correo institucional.");
        }
        Usuario u = usuarios.findById(t.getUsuarioId())
                .orElseThrow(() -> ApiException.solicitudInvalida("ENLACE_INVALIDO", "El enlace no es válido."));
        t.setUsado(true);
        tokens.save(t);
        if (requiereAdministrador(u, LocalDateTime.now())) {
            // El administrador lo bloqueó (o se superó el límite de bloqueos): el enlace ya no sirve.
            auditoria.registrar(u.getId(), "DESBLOQUEO_ENLACE_INVALIDO", "requiere administrador",
                    ResultadoAuditoria.DENEGADO, ip);
            throw ApiException.prohibido("DESBLOQUEO_REQUIERE_ADMINISTRADOR",
                    "Tu cuenta solo la puede desbloquear un administrador. Escríbele para recuperar el acceso.");
        }
        u.desbloquear();
        usuarios.save(u);
        auditoria.registrar(u.getId(), "CUENTA_DESBLOQUEADA_POR_ENLACE", "usuarios/" + u.getId(),
                ResultadoAuditoria.PERMITIDO, ip);
        return new MensajeRespuesta("Tu cuenta está desbloqueada. Ya puedes iniciar sesión.");
    }

    private String emitirEnlace(Usuario u, String ip) {
        tokens.invalidarPendientes(u.getId());
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        TokenDesbloqueo t = new TokenDesbloqueo();
        t.setUsuarioId(u.getId());
        t.setTokenHash(Hashes.sha256Hex(token));
        t.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA));
        t.setIpOrigen(ip);
        tokens.save(t);

        String enlace = urlBase + "?token=" + token;
        // El enlace va solo por correo: nunca se guarda en la bandeja de notificaciones.
        correo.enviar(u.getCorreoInstitucional(), "Desbloquea tu cuenta de V-Learning",
                "Tu cuenta se bloqueó por varios intentos fallidos de inicio de sesión. Usa este enlace para "
                        + "desbloquearla. Es de un solo uso y vence en " + MINUTOS_VIGENCIA + " minutos:\n\n" + enlace
                        + "\n\nSi no fuiste tú, cambia tu contraseña cuando entres.");
        return enlace;
    }

    private void exigirAdministrador(Usuario u, String ip) {
        tokens.invalidarPendientes(u.getId());
        if (u.getEstado() == EstadoUsuario.ACTIVO || u.getEstado() == EstadoUsuario.BLOQUEADO) {
            u.setEstado(EstadoUsuario.BLOQUEADO);
            u.setBloqueadoHasta(null); // bloqueo sin vencimiento: solo lo levanta un administrador
        } else {
            // Primer acceso pendiente: se conserva el estado y el bloqueo dura hasta que actúe el administrador.
            u.setBloqueadoHasta(LocalDateTime.now().plusYears(100));
        }
        usuarios.save(u);
        auditoria.registrar(u.getId(), "BLOQUEO_REQUIERE_ADMINISTRADOR",
                BLOQUEOS_PARA_ADMINISTRADOR + " bloqueos en " + HORAS_VENTANA_BLOQUEOS + " h",
                ResultadoAuditoria.DENEGADO, ip);
        notificaciones.notificarAdministradores("Cuenta bloqueada: requiere desbloqueo manual",
                u.getCorreoInstitucional() + " se bloqueó " + BLOQUEOS_PARA_ADMINISTRADOR
                        + " veces en 24 horas. Revisa la actividad y desbloquéala si corresponde.", "/usuarios");
        correo.enviar(u.getCorreoInstitucional(), "Tu cuenta de V-Learning está bloqueada",
                "Tu cuenta se bloqueó " + BLOQUEOS_PARA_ADMINISTRADOR + " veces en 24 horas por intentos fallidos. "
                        + "Por seguridad, ahora solo un administrador puede desbloquearla. Ya le avisamos.");
    }
}
