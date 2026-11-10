package com.elearning.platform.services;

import com.elearning.platform.entity.Estudiante;
import com.elearning.platform.entity.Notificacion;
import com.elearning.platform.entity.PerfilGamificacion;
import com.elearning.platform.enums.CanalNotificacion;
import com.elearning.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-047 (HU-047): resumen diario de notificaciones no urgentes y pausa de la gamificación. */
class ResumenNotificacionesTest {

    private NotificacionRepository repo;
    private MailService correo;
    private NotificacionService servicio;
    private Estudiante ana;

    @BeforeEach
    void preparar() {
        repo = mock(NotificacionRepository.class);
        correo = mock(MailService.class);
        when(correo.enviar(any(), any(), any())).thenReturn(true);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        servicio = new NotificacionService(repo, mock(UsuarioRepository.class), correo, mock(InscripcionRepository.class));
        ana = new Estudiante();
        ana.setId(20L);
        ana.setCorreoInstitucional("ana@tdea.edu.co");
        ana.setCanalPreferido(CanalNotificacion.CORREO);
        ana.setResumenDiario(true);
        ana.setHoraResumen(LocalTime.of(18, 30));
    }

    @Test
    void laHoraDelResumenEsHoySiNoHaPasadoYSiNoManana() {
        LocalDateTime manana10 = LocalDateTime.of(2026, 11, 10, 10, 0);
        assertEquals(LocalDateTime.of(2026, 11, 10, 18, 30), NotificacionService.proximoResumen(LocalTime.of(18, 30), manana10));
        assertEquals(LocalDateTime.of(2026, 11, 11, 18, 30),
                NotificacionService.proximoResumen(LocalTime.of(18, 30), LocalDateTime.of(2026, 11, 10, 19, 0)));
        assertEquals(LocalDateTime.of(2026, 11, 10, 18, 0), NotificacionService.proximoResumen(null, manana10));
    }

    @Test
    void unaFechaLimiteEnMenosDe24HorasEsUrgente() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 10, 10, 0);
        assertTrue(NotificacionService.esUrgente(ahora.plusHours(23), ahora));
        assertFalse(NotificacionService.esUrgente(ahora.plusHours(30), ahora));
        assertFalse(NotificacionService.esUrgente(null, ahora));
    }

    @Test
    void loNoUrgenteSeGuardaParaElResumenSinEnviarCorreo() {
        Notificacion n = servicio.notificar(ana, "Nuevo contenido", "Hay una lectura nueva.", false, null, false).orElseThrow();

        assertTrue(n.isEnResumen());
        assertNotNull(n.getPospuestaHasta());
        assertEquals(LocalTime.of(18, 30), n.getPospuestaHasta().toLocalTime());
        verify(correo, never()).enviar(any(), any(), any());
    }

    @Test
    void loUrgenteSeEnviaDeInmediatoAunqueHayaResumen() {
        Notificacion n = servicio.notificar(ana, "Vence hoy", "La evaluación vence en 3 horas.", false, null, true).orElseThrow();

        assertFalse(n.isEnResumen());
        assertNull(n.getPospuestaHasta());
        assertTrue(n.isUrgente());
        verify(correo).enviar(eq("ana@tdea.edu.co"), eq("Vence hoy"), any());
    }

    @Test
    void loObligatorioNuncaVaAlResumen() {
        Notificacion n = servicio.notificar(ana, "Cuenta bloqueada", "x", true, null, false).orElseThrow();
        assertFalse(n.isEnResumen());
    }

    @Test
    void sinResumenTodoSeEnviaComoAntes() {
        ana.setResumenDiario(false);
        Notificacion n = servicio.notificar(ana, "Nuevo contenido", "x", false, null, false).orElseThrow();
        assertFalse(n.isEnResumen());
        verify(correo).enviar(any(), any(), any());
    }

    @Test
    void elResumenJuntaLosAvisosEnUnaNotificacionYUnCorreo() {
        Notificacion a = pendiente("Nuevo contenido", 1);
        Notificacion b = pendiente("Nueva tarea", 2);
        when(repo.paraResumen(any())).thenReturn(List.of(b, a));

        new ResumenDiarioJob(repo, correo).enviar();

        ArgumentCaptor<Notificacion> guardada = ArgumentCaptor.forClass(Notificacion.class);
        verify(repo).save(guardada.capture());
        assertEquals("Tu resumen diario: 2 aviso(s)", guardada.getValue().getTitulo());
        assertTrue(guardada.getValue().getMensaje().indexOf("Nuevo contenido") < guardada.getValue().getMensaje().indexOf("Nueva tarea"));
        verify(correo, times(1)).enviar(eq("ana@tdea.edu.co"), any(), any());
        assertFalse(a.isEnResumen());
        assertFalse(b.isEnResumen());
    }

    @Test
    void elProcesoCorreCadaCincoMinutosParaCumplirElMargen() throws Exception {
        var cron = ResumenDiarioJob.class.getMethod("enviar")
                .getAnnotation(org.springframework.scheduling.annotation.Scheduled.class).cron();
        assertTrue(cron.contains("0 */5 * * * *"));
    }

    @Test
    void conLaGamificacionEnPausaLosPuntosSeRegistranPeroNoSeAvisan() {
        PerfilGamificacionRepository perfiles = mock(PerfilGamificacionRepository.class);
        InsigniaRepository insignias = mock(InsigniaRepository.class);
        NotificacionService notificaciones = mock(NotificacionService.class);
        PerfilGamificacion p = new PerfilGamificacion();
        p.setAvisosPausados(true);
        when(perfiles.findByEstudianteId(20L)).thenReturn(Optional.of(p));
        when(insignias.findAll()).thenReturn(List.of());
        GamificacionService g = new GamificacionService(perfiles, insignias, mock(InsigniaObtenidaRepository.class),
                mock(ReglaGamificacionRepository.class), mock(EstudianteRepository.class), notificaciones,
                mock(HistorialPuntosRepository.class));

        g.otorgar(20L, 500, "Completaste un curso");

        assertEquals(500, p.getPuntos());
        verifyNoInteractions(notificaciones);
    }

    private Notificacion pendiente(String titulo, int minuto) {
        Notificacion n = new Notificacion();
        n.setUsuario(ana);
        n.setTitulo(titulo);
        n.setMensaje("detalle");
        n.setEnResumen(true);
        n.setFechaCreacion(LocalDateTime.of(2026, 11, 10, 9, minuto));
        return n;
    }
}
