package com.elearning.platform.services;

import com.elearning.platform.config.VlearningProperties;
import com.elearning.platform.entity.Estudiante;
import com.elearning.platform.entity.TokenDesbloqueo;
import com.elearning.platform.entity.Usuario;
import com.elearning.platform.enums.EstadoUsuario;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.RegistroAuditoriaRepository;
import com.elearning.platform.repository.TokenDesbloqueoRepository;
import com.elearning.platform.repository.UsuarioRepository;
import com.elearning.platform.util.Hashes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-043 (HU-043): enlace de desbloqueo de un solo uso, 15 minutos y 3 bloqueos en 24 h. */
class DesbloqueoServiceTest {

    private UsuarioRepository usuarios;
    private TokenDesbloqueoRepository tokens;
    private RegistroAuditoriaRepository registros;
    private MailService correo;
    private NotificacionService notificaciones;
    private AuditoriaService auditoria;
    private DesbloqueoService servicio;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        tokens = mock(TokenDesbloqueoRepository.class);
        registros = mock(RegistroAuditoriaRepository.class);
        correo = mock(MailService.class);
        notificaciones = mock(NotificacionService.class);
        auditoria = mock(AuditoriaService.class);
        VlearningProperties props = mock(VlearningProperties.class);
        when(props.dev()).thenReturn(new VlearningProperties.Dev(true, false));
        servicio = new DesbloqueoService(usuarios, tokens, registros, correo, notificaciones, auditoria, props);
        ReflectionTestUtils.setField(servicio, "urlBase", "http://localhost:5173/desbloquear");

        usuario = new Estudiante();
        usuario.setId(7L);
        usuario.setCorreoInstitucional("ana@tdea.edu.co");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.bloquear(LocalDateTime.now().plusMinutes(15));
        when(usuarios.findById(7L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void desdeElTercerBloqueoEn24HorasDesbloqueaElAdministrador() {
        assertFalse(DesbloqueoService.requiereAdministrador(2));
        assertTrue(DesbloqueoService.requiereAdministrador(3));
    }

    @Test
    void alBloquearseSeEnviaUnEnlaceQueVenceEnQuinceMinutos() {
        when(registros.countByUsuarioIdAndAccionAndResultadoAndFechaAfter(eq(7L), eq("BLOQUEO_TEMPORAL"),
                eq(ResultadoAuditoria.DENEGADO), any())).thenReturn(1L);

        Optional<String> enlace = servicio.alBloquear(usuario, "127.0.0.1");

        assertTrue(enlace.isPresent());
        assertTrue(enlace.get().startsWith("http://localhost:5173/desbloquear?token="));
        ArgumentCaptor<TokenDesbloqueo> guardado = ArgumentCaptor.forClass(TokenDesbloqueo.class);
        verify(tokens).save(guardado.capture());
        long minutos = Duration.between(LocalDateTime.now(), guardado.getValue().getFechaExpiracion()).toMinutes();
        assertTrue(minutos >= 14 && minutos <= 15, "vence a los 15 minutos");
        verify(correo).enviar(eq("ana@tdea.edu.co"), anyString(), contains(enlace.get()));
        // El enlace nunca se guarda en la bandeja de notificaciones.
        verify(notificaciones).notificar(eq(7L), anyString(), argThat(m -> !m.contains("token=")), eq(true));
    }

    @Test
    void elTercerBloqueoNoEnviaEnlaceYDejaLaCuentaParaElAdministrador() {
        when(registros.countByUsuarioIdAndAccionAndResultadoAndFechaAfter(eq(7L), eq("BLOQUEO_TEMPORAL"),
                eq(ResultadoAuditoria.DENEGADO), any())).thenReturn(3L);

        assertTrue(servicio.alBloquear(usuario, "127.0.0.1").isEmpty());

        assertEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());
        assertNull(usuario.getBloqueadoHasta());
        verify(tokens, never()).save(any());
        verify(auditoria).registrar(eq(7L), eq("BLOQUEO_REQUIERE_ADMINISTRADOR"), anyString(),
                eq(ResultadoAuditoria.DENEGADO), anyString());
        verify(notificaciones).notificarAdministradores(anyString(), anyString(), eq("/usuarios"));
    }

    @Test
    void elEnlaceDesbloqueaUnaSolaVezYQuedaEnAuditoria() {
        TokenDesbloqueo t = token(LocalDateTime.now().plusMinutes(10));
        when(tokens.findByTokenHash(Hashes.sha256Hex("abc"))).thenReturn(Optional.of(t));

        servicio.desbloquear("abc", "127.0.0.1");

        assertTrue(t.isUsado());
        assertFalse(usuario.estaBloqueadoTemporalmente());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        verify(auditoria).registrar(eq(7L), eq("CUENTA_DESBLOQUEADA_POR_ENLACE"), anyString(),
                eq(ResultadoAuditoria.PERMITIDO), anyString());

        ApiException segundo = assertThrows(ApiException.class, () -> servicio.desbloquear("abc", "127.0.0.1"));
        assertEquals("ENLACE_INVALIDO", segundo.getCodigo());
    }

    @Test
    void unEnlaceVencidoNoSirve() {
        when(tokens.findByTokenHash(Hashes.sha256Hex("viejo")))
                .thenReturn(Optional.of(token(LocalDateTime.now().minusMinutes(1))));

        ApiException e = assertThrows(ApiException.class, () -> servicio.desbloquear("viejo", "127.0.0.1"));

        assertEquals("ENLACE_INVALIDO", e.getCodigo());
        assertTrue(usuario.estaBloqueadoTemporalmente());
    }

    @Test
    void elEnlaceNoLevantaUnBloqueoQueSoloPuedeQuitarElAdministrador() {
        usuario.setEstado(EstadoUsuario.BLOQUEADO);
        usuario.setBloqueadoHasta(null);
        when(tokens.findByTokenHash(Hashes.sha256Hex("abc")))
                .thenReturn(Optional.of(token(LocalDateTime.now().plusMinutes(10))));

        ApiException e = assertThrows(ApiException.class, () -> servicio.desbloquear("abc", "127.0.0.1"));

        assertEquals(HttpStatus.FORBIDDEN, e.getEstado());
        assertEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());
    }

    @Test
    void sePuedePedirOtroEnlaceTrasUnMinuto() {
        LocalDateTime ahora = LocalDateTime.now();
        assertTrue(DesbloqueoService.puedeEnviarOtro(null, ahora));
        assertFalse(DesbloqueoService.puedeEnviarOtro(ahora.minusSeconds(20), ahora));
        assertTrue(DesbloqueoService.puedeEnviarOtro(ahora.minusSeconds(61), ahora));
    }

    private TokenDesbloqueo token(LocalDateTime expira) {
        TokenDesbloqueo t = new TokenDesbloqueo();
        t.setUsuarioId(7L);
        t.setTokenHash("x");
        t.setFechaExpiracion(expira);
        return t;
    }
}
