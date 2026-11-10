package com.elearning.platform.services;

import com.elearning.platform.dto.AccesibilidadDtos.ConfiguracionDto;
import com.elearning.platform.dto.AcomodacionDtos.AcomodacionRequest;
import com.elearning.platform.entity.*;
import com.elearning.platform.enums.EstadoInscripcion;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.RolUsuario;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.AcomodacionRepository;
import com.elearning.platform.repository.CursoRepository;
import com.elearning.platform.repository.InscripcionRepository;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.util.TiempoEvaluacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-041 (HU-041): asignación, permisos, vigencia y auditoría de las acomodaciones. */
class AcomodacionServiceTest {

    private AcomodacionRepository acomodaciones;
    private InscripcionRepository inscripciones;
    private AccesibilidadService accesibilidad;
    private NotificacionService notificaciones;
    private AuditoriaService auditoria;
    private AcomodacionService servicio;
    private Curso curso;
    private Inscripcion inscripcion;

    private final UsuarioPrincipal instructor = new UsuarioPrincipal(10L, "i@tdea.edu.co", "Ines", RolUsuario.INSTRUCTOR, 1L);
    private final UsuarioPrincipal otroInstructor = new UsuarioPrincipal(11L, "o@tdea.edu.co", "Otro", RolUsuario.INSTRUCTOR, 2L);
    private final UsuarioPrincipal admin = new UsuarioPrincipal(1L, "a@tdea.edu.co", "Admin", RolUsuario.ADMINISTRADOR, 3L);

    @BeforeEach
    void preparar() {
        acomodaciones = mock(AcomodacionRepository.class);
        inscripciones = mock(InscripcionRepository.class);
        CursoRepository cursos = mock(CursoRepository.class);
        accesibilidad = mock(AccesibilidadService.class);
        notificaciones = mock(NotificacionService.class);
        auditoria = mock(AuditoriaService.class);
        servicio = new AcomodacionService(acomodaciones, inscripciones, cursos, accesibilidad, notificaciones, auditoria);

        Instructor duenio = new Instructor();
        duenio.setId(10L);
        curso = new Curso();
        curso.setId(5L);
        curso.setTitulo("Bases de datos");
        curso.setInstructor(duenio);
        when(cursos.findById(5L)).thenReturn(Optional.of(curso));

        Estudiante estudiante = new Estudiante();
        estudiante.setId(20L);
        estudiante.setNombre("Sofía");
        inscripcion = new Inscripcion();
        inscripcion.setId(30L);
        inscripcion.setEstudiante(estudiante);
        inscripcion.setCurso(curso);
        inscripcion.setEstado(EstadoInscripcion.ACTIVA);
        when(inscripciones.findByEstudianteIdAndCursoId(20L, 5L)).thenReturn(Optional.of(inscripcion));
        when(acomodaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(accesibilidad.configuracionDe(20L)).thenReturn(config(25));
    }

    @Test
    void soloSeAceptanMultiplicadoresDe15y2() {
        assertEquals(new BigDecimal("1.50"), AcomodacionService.normalizarMultiplicador(new BigDecimal("1.5")));
        assertEquals(new BigDecimal("2.00"), AcomodacionService.normalizarMultiplicador(new BigDecimal("2")));
        assertNull(AcomodacionService.normalizarMultiplicador(null));
        assertThrows(ApiException.class, () -> AcomodacionService.normalizarMultiplicador(new BigDecimal("1.25")));
    }

    @Test
    void elInstructorDelCursoAsignaYQuedaEnAuditoria() {
        var vista = servicio.asignar(5L, pedido(new BigDecimal("1.5"), null), instructor, "127.0.0.1");

        assertEquals(new BigDecimal("1.50"), vista.multiplicadorTiempo());
        verify(auditoria).registrar(eq(10L), eq("ACOMODACION_ASIGNADA"), anyString(), eq(ResultadoAuditoria.PERMITIDO), anyString());
    }

    @Test
    void elAdministradorTambienPuedeAsignar() {
        assertDoesNotThrow(() -> servicio.asignar(5L, pedido(new BigDecimal("2"), null), admin, "127.0.0.1"));
    }

    @Test
    void otroInstructorNoPuedeAsignar() {
        ApiException e = assertThrows(ApiException.class,
                () -> servicio.asignar(5L, pedido(new BigDecimal("2"), null), otroInstructor, "127.0.0.1"));
        assertEquals("ACCESO_DENEGADO", e.getCodigo());
        verify(acomodaciones, never()).save(any());
    }

    @Test
    void laNuevaAcomodacionReemplazaALaAnterior() {
        Acomodacion previa = new Acomodacion();
        previa.setInscripcion(inscripcion);
        when(acomodaciones.findFirstByInscripcionIdAndActivaTrue(30L)).thenReturn(Optional.of(previa));

        servicio.asignar(5L, pedido(new BigDecimal("2"), null), instructor, "127.0.0.1");

        assertFalse(previa.isActiva());
    }

    @Test
    void unaAcomodacionVaciaSeRechaza() {
        ApiException e = assertThrows(ApiException.class,
                () -> servicio.asignar(5L, pedido(null, null), instructor, "127.0.0.1"));
        assertEquals("ACOMODACION_VACIA", e.getCodigo());
    }

    @Test
    void seAplicaATodasLasEvaluacionesDelCursoConElMayorDeLosDos() {
        Acomodacion a = new Acomodacion();
        a.setInscripcion(inscripcion);
        a.setMultiplicadorTiempo(new BigDecimal("1.50"));
        when(acomodaciones.activaDe(20L, 5L)).thenReturn(Optional.of(a));

        TiempoEvaluacion t = servicio.tiempoPara(20L, curso);

        // Perfil +25 % y acomodación 1,5x: 40 min de base → 60 (el mayor), no 70 (la suma).
        assertEquals(60, t.minutosLimite(40));
    }

    @Test
    void unaAcomodacionVencidaNoSeAplicaYSeAvisaAlInstructor() {
        Acomodacion a = new Acomodacion();
        a.setInscripcion(inscripcion);
        a.setTiempoIlimitado(true);
        a.setVigenteHasta(LocalDate.now().minusDays(1));
        when(acomodaciones.activaDe(20L, 5L)).thenReturn(Optional.of(a));

        TiempoEvaluacion t = servicio.tiempoPara(20L, curso);

        assertEquals(50, t.minutosLimite(40)); // solo el perfil (+25 %)
        verify(notificaciones).notificarUnaVezAlDia(eq(10L), contains("vencida"), anyString(), eq("/cursos/5/notas"));
    }

    @Test
    void laVigenciaIncluyeElUltimoDia() {
        Acomodacion a = new Acomodacion();
        a.setVigenteHasta(LocalDate.of(2026, 11, 30));
        assertFalse(a.vencida(LocalDate.of(2026, 11, 30)));
        assertTrue(a.vencida(LocalDate.of(2026, 12, 1)));
    }

    private static AcomodacionRequest pedido(BigDecimal multiplicador, Boolean ilimitado) {
        return new AcomodacionRequest(20L, multiplicador, ilimitado, 0, null, 0, null);
    }

    private static ConfiguracionDto config(int extra) {
        return new ConfiguracionDto(false, 16, null, new BigDecimal("1.50"), false, false, false, false, false, extra);
    }
}
