package com.elearning.platform.services;

import com.elearning.platform.config.VlearningProperties;
import com.elearning.platform.entity.CodigoOtp;
import com.elearning.platform.entity.Usuario;
import com.elearning.platform.repository.CodigoOtpRepository;
import com.elearning.platform.util.Hashes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Segundo factor (RF-002): código de 6 dígitos guardado solo como hash.
 * RF-044: el código vale al menos 10 minutos y se puede reenviar con límites.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** RF-044: vigencia mínima del código, aunque la propiedad configure menos. */
    static final int MINUTOS_MINIMOS_VIGENCIA = 10;
    /** RF-044: segundos de espera entre un código y el siguiente. */
    static final int SEGUNDOS_ENTRE_REENVIOS = 60;
    /** RF-044: reenvíos permitidos en la ventana; el reenvío solo vale para un inicio de sesión reciente. */
    static final int MAX_REENVIOS = 5;
    static final int VENTANA_REENVIO_MINUTOS = 30;

    /** RF-044: resultado de la política de reenvío. */
    public enum Reenvio { PERMITIDO, SIN_DESAFIO, ESPERAR, LIMITE }

    private final CodigoOtpRepository repositorio;
    private final VlearningProperties props;

    /** Genera un código nuevo e invalida los anteriores. Devuelve el código en claro para enviarlo. */
    @Transactional
    public String generar(Usuario usuario, String ip) {
        repositorio.invalidarPendientes(usuario.getId());
        String codigo = String.format("%06d", ALEATORIO.nextInt(1_000_000));
        CodigoOtp otp = new CodigoOtp();
        otp.setUsuarioId(usuario.getId());
        otp.setCodigoHash(hash(usuario.getId(), codigo));
        otp.setFechaExpiracion(LocalDateTime.now().plusMinutes(minutosVigencia()));
        otp.setIpOrigen(ip);
        repositorio.save(otp);
        return codigo;
    }

    /** true si el código es correcto y vigente; lo consume. */
    @Transactional
    public boolean verificar(Usuario usuario, String codigo) {
        Optional<CodigoOtp> pendiente = repositorio.findFirstByUsuarioIdAndUsadoFalseOrderByFechaCreacionDesc(usuario.getId());
        if (pendiente.isEmpty() || !pendiente.get().vigente()) return false;
        CodigoOtp otp = pendiente.get();
        if (!Hashes.iguales(otp.getCodigoHash(), hash(usuario.getId(), codigo))) return false;
        otp.setUsado(true);
        return true;
    }

    @Transactional
    public void invalidar(Usuario usuario) {
        repositorio.invalidarPendientes(usuario.getId());
    }

    /** Fecha de creación del último código pendiente (para contar intentos fallidos desde entonces). */
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> inicioCodigoVigente(Usuario usuario) {
        return repositorio.findFirstByUsuarioIdAndUsadoFalseOrderByFechaCreacionDesc(usuario.getId())
                .map(CodigoOtp::getFechaCreacion);
    }

    /** RF-044: minutos que vale cada código (mínimo 10). */
    public int minutosVigencia() {
        return minutosVigencia(props.otp().minutosValidez());
    }

    static int minutosVigencia(int configurados) {
        return Math.max(configurados, MINUTOS_MINIMOS_VIGENCIA);
    }

    /**
     * RF-044: decide si se puede enviar otro código. Solo hay reenvío si existe un código pendiente
     * creado hace menos de 30 minutos (es decir, la persona acaba de validar su contraseña), con 60 s
     * de espera entre códigos y como máximo 5 reenvíos en la ventana.
     */
    static Reenvio politicaReenvio(LocalDateTime creacionPendiente, long reenviosRecientes, LocalDateTime ahora) {
        if (creacionPendiente == null || creacionPendiente.isBefore(ahora.minusMinutes(VENTANA_REENVIO_MINUTOS))) {
            return Reenvio.SIN_DESAFIO;
        }
        if (creacionPendiente.isAfter(ahora.minusSeconds(SEGUNDOS_ENTRE_REENVIOS))) return Reenvio.ESPERAR;
        if (reenviosRecientes >= MAX_REENVIOS) return Reenvio.LIMITE;
        return Reenvio.PERMITIDO;
    }

    /** HMAC-like: el secreto del servidor evita recuperar el código de 6 dígitos a partir de la base. */
    private String hash(Long usuarioId, String codigo) {
        return Hashes.sha256Hex(usuarioId + ":" + codigo + ":" + props.jwt().secret());
    }
}
