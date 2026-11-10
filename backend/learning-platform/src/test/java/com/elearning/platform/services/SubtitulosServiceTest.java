package com.elearning.platform.services;

import com.elearning.platform.dto.SubtitulosDtos.LineaDto;
import com.elearning.platform.entity.*;
import com.elearning.platform.entity.SubtituloBorrador.Estado;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.TipoRecursoAccesible;
import com.elearning.platform.events.SubtitulosSolicitadosEvent;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.ContenidoRepository;
import com.elearning.platform.repository.RecursoAccesibleRepository;
import com.elearning.platform.repository.SubtituloBorradorRepository;
import com.elearning.platform.services.transcripcion.ServicioTranscripcion;
import com.elearning.platform.services.transcripcion.TranscripcionPrueba;
import com.elearning.platform.util.Vtt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-037 (HU-037): borrador automático, edición, archivo manual y aprobación obligatoria. */
class SubtitulosServiceTest {

    private SubtituloBorradorRepository borradores;
    private RecursoAccesibleRepository recursos;
    private ApplicationEventPublisher eventos;
    private SubtitulosService servicio;
    private Contenido video;
    private final List<RecursoAccesible> guardados = new ArrayList<>();

    @BeforeEach
    void preparar() {
        borradores = mock(SubtituloBorradorRepository.class);
        recursos = mock(RecursoAccesibleRepository.class);
        eventos = mock(ApplicationEventPublisher.class);
        AccesoService acceso = mock(AccesoService.class);
        servicio = new SubtitulosService(borradores, recursos, acceso, mock(AuditoriaService.class), eventos);
        video = contenido(1L, FormatoContenido.VIDEO);
        when(acceso.contenidoPropio(1L, 10L)).thenReturn(video);
        when(borradores.save(any())).thenAnswer(i -> i.getArgument(0));
        when(recursos.save(any())).thenAnswer(i -> { guardados.add(i.getArgument(0)); return i.getArgument(0); });
    }

    private static Contenido contenido(Long id, FormatoContenido f) {
        Curso curso = new Curso();
        curso.setId(5L);
        Modulo m = new Modulo();
        m.setCurso(curso);
        Contenido c = new Contenido();
        c.setId(id);
        c.setTitulo("Clase 1");
        c.setFormato(f);
        c.setUrlRecurso("https://x/clase.mp4");
        c.setModulo(m);
        return c;
    }

    private SubtituloBorrador borrador(Estado estado) {
        SubtituloBorrador b = new SubtituloBorrador();
        b.setContenidoId(1L);
        b.setEstado(estado);
        b.setUrlMedio(video.getUrlRecurso());
        b.setVtt(Vtt.aVtt(List.of(new Vtt.Linea(0, 3000, "Hola a todos"))));
        when(borradores.findByContenidoId(1L)).thenReturn(Optional.of(b));
        return b;
    }

    @Test
    void alGuardarUnVideoNuevoSePideElBorradorEnSegundoPlano() {
        when(borradores.findByContenidoId(1L)).thenReturn(Optional.empty());
        when(recursos.findByContenidoIdOrderByIdAsc(1L)).thenReturn(List.of());

        servicio.alGuardarContenido(video, 10L);

        verify(eventos).publishEvent(new SubtitulosSolicitadosEvent(1L, 10L));
    }

    @Test
    void noSePideOtroBorradorSiYaHaySubtitulosOElMedioNoCambio() {
        RecursoAccesible sub = new RecursoAccesible();
        sub.setTipo(TipoRecursoAccesible.SUBTITULO);
        sub.setDisponible(true);
        when(recursos.findByContenidoIdOrderByIdAsc(1L)).thenReturn(List.of(sub));
        servicio.alGuardarContenido(video, 10L);

        when(recursos.findByContenidoIdOrderByIdAsc(1L)).thenReturn(List.of());
        borrador(Estado.LISTO);
        servicio.alGuardarContenido(video, 10L);

        servicio.alGuardarContenido(contenido(2L, FormatoContenido.LECTURA), 10L);
        verify(eventos, never()).publishEvent(any());
    }

    @Test
    void aprobarUnVideoCreaLosSubtitulosYLaTranscripcion() {
        borrador(Estado.LISTO);
        when(recursos.findByContenidoIdOrderByIdAsc(1L)).thenReturn(List.of());

        var vista = servicio.aprobar(1L, 10L, "127.0.0.1");

        assertEquals("APROBADO", vista.estado());
        RecursoAccesible sub = guardados.stream().filter(r -> r.getTipo() == TipoRecursoAccesible.SUBTITULO).findFirst().orElseThrow();
        assertEquals("/api/archivos/subtitulos/1.vtt", sub.getUrl());
        RecursoAccesible tr = guardados.stream().filter(r -> r.getTipo() == TipoRecursoAccesible.TRANSCRIPCION).findFirst().orElseThrow();
        assertEquals("Hola a todos", tr.getDescripcion());
    }

    @Test
    void unPodcastAprobadoGanaLaTranscripcionYNoSubtitulos() {
        Contenido podcast = contenido(1L, FormatoContenido.PODCAST);
        AccesoService acceso = mock(AccesoService.class);
        when(acceso.contenidoPropio(1L, 10L)).thenReturn(podcast);
        servicio = new SubtitulosService(borradores, recursos, acceso, mock(AuditoriaService.class), eventos);
        borrador(Estado.LISTO);

        servicio.aprobar(1L, 10L, "127.0.0.1");

        assertTrue(guardados.stream().noneMatch(r -> r.getTipo() == TipoRecursoAccesible.SUBTITULO));
        assertTrue(guardados.stream().anyMatch(r -> r.getTipo() == TipoRecursoAccesible.TRANSCRIPCION));
    }

    @Test
    void sinBorradorListoNoSePuedeAprobar() {
        borrador(Estado.GENERANDO);
        ApiException e = assertThrows(ApiException.class, () -> servicio.aprobar(1L, 10L, "127.0.0.1"));
        assertEquals("BORRADOR_NO_LISTO", e.getCodigo());
        verify(recursos, never()).save(any());
    }

    @Test
    void editarUnBorradorAprobadoObligaAAprobarloDeNuevo() {
        SubtituloBorrador b = borrador(Estado.APROBADO);

        var vista = servicio.editar(1L, 10L, List.of(new LineaDto(0L, 2000L, "Hola, bienvenidos")), "127.0.0.1");

        assertEquals("LISTO", vista.estado());
        assertEquals(Estado.LISTO, b.getEstado());
        assertEquals("Hola, bienvenidos", vista.lineas().get(0).texto());
    }

    @Test
    void lasLineasConTiemposInvalidosSeRechazan() {
        borrador(Estado.LISTO);
        assertThrows(ApiException.class,
                () -> servicio.editar(1L, 10L, List.of(new LineaDto(5000L, 1000L, "x")), "127.0.0.1"));
    }

    @Test
    void siFallaElReconocimientoSePuedeSubirUnSrt() {
        when(borradores.findByContenidoId(1L)).thenReturn(Optional.empty());
        var srt = new MockMultipartFile("archivo", "clase.srt", "application/x-subrip",
                "1\n00:00:00,000 --> 00:00:02,500\nHola\n".getBytes());

        var vista = servicio.subirArchivo(1L, 10L, srt, "127.0.0.1");

        assertEquals("LISTO", vista.estado());
        assertEquals("MANUAL", vista.origen());
        assertEquals(2500L, vista.lineas().get(0).finMs());
    }

    @Test
    void soloSeAceptanArchivosSrtOVtt() {
        var pdf = new MockMultipartFile("archivo", "clase.pdf", "application/pdf", new byte[]{1});
        ApiException e = assertThrows(ApiException.class, () -> servicio.subirArchivo(1L, 10L, pdf, "127.0.0.1"));
        assertEquals("ARCHIVO_FORMATO", e.getCodigo());
    }

    @Test
    void elVttSoloSeSirveCuandoEstaAprobado() {
        borrador(Estado.LISTO);
        assertTrue(servicio.vttAprobado(1L).isEmpty());
        borrador(Estado.APROBADO);
        assertTrue(servicio.vttAprobado(1L).isPresent());
    }

    @Test
    void laGeneracionDejaElBorradorListoYAvisaSinPublicarNada() {
        SubtituloBorrador b = borrador(Estado.PENDIENTE);
        ContenidoRepository contenidos = mock(ContenidoRepository.class);
        when(contenidos.findById(1L)).thenReturn(Optional.of(video));
        NotificacionService notificaciones = mock(NotificacionService.class);
        new SubtitulosGeneracion(borradores, contenidos, new TranscripcionPrueba(), notificaciones).generar(1L, 10L);

        assertEquals(Estado.LISTO, b.getEstado());
        assertFalse(Vtt.leer(b.getVtt()).isEmpty());
        verify(notificaciones).notificar(eq(10L), contains("listo"), contains("cuando lo apruebes"), eq(true), eq("/cursos/5/editar"));
        verifyNoInteractions(recursos);
    }

    @Test
    void siElReconocimientoFallaQuedaFallidoYSeOfreceSubirArchivo() {
        SubtituloBorrador b = borrador(Estado.PENDIENTE);
        ContenidoRepository contenidos = mock(ContenidoRepository.class);
        when(contenidos.findById(1L)).thenReturn(Optional.of(video));
        ServicioTranscripcion falla = mock(ServicioTranscripcion.class);
        when(falla.transcribir(any())).thenThrow(new IllegalStateException("sin audio"));
        NotificacionService notificaciones = mock(NotificacionService.class);

        new SubtitulosGeneracion(borradores, contenidos, falla, notificaciones).generar(1L, 10L);

        assertEquals(Estado.FALLIDO, b.getEstado());
        assertTrue(b.getError().contains(".srt"));
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        verify(notificaciones).notificar(eq(10L), anyString(), msg.capture(), eq(true), anyString());
        assertTrue(msg.getValue().contains(".srt"));
    }
}
