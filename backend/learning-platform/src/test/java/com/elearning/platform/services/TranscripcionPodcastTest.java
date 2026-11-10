package com.elearning.platform.services;

import com.elearning.platform.dto.VerificacionDtos.Hallazgo;
import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.RecursoAccesible;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.SeveridadHallazgo;
import com.elearning.platform.enums.TipoRecursoAccesible;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RF-039 (HU-039): no se publica un podcast o audio grabado sin transcripción. */
class TranscripcionPodcastTest {

    @Test
    void unPodcastSinTranscripcionBloqueaLaPublicacion() {
        List<Hallazgo> lista = AccesibilidadContenidoService.analizar(FormatoContenido.PODCAST, "https://a.mp3", null,
                EnumSet.noneOf(TipoRecursoAccesible.class));

        Hallazgo h = lista.stream().filter(x -> x.regla().equals("AUDIO_SIN_TRANSCRIPCION")).findFirst().orElseThrow();
        assertEquals(SeveridadHallazgo.BLOQUEANTE, h.severidad());
        assertEquals("1.2.1", h.criterioWcag());
        assertFalse(h.comoCorregir().isBlank());
        // HU-039 criterio 4: se ofrece generar el borrador automático (RF-037).
        assertTrue(h.comoCorregir().contains("borrador automático"));
    }

    /** HU-039 criterio 4: estas reglas permiten ofrecer el botón «Generar borrador automático». */
    @Test
    void lasReglasDeMediosOfrecenElBorradorAutomatico() {
        assertTrue(AccesibilidadContenidoService.ofreceBorradorAutomatico("AUDIO_SIN_TRANSCRIPCION"));
        assertTrue(AccesibilidadContenidoService.ofreceBorradorAutomatico("VIDEO_SIN_SUBTITULOS"));
        assertFalse(AccesibilidadContenidoService.ofreceBorradorAutomatico("IMAGEN_SIN_ALT"));
    }

    @Test
    void unPodcastConTranscripcionSePuedePublicar() {
        List<Hallazgo> lista = AccesibilidadContenidoService.analizar(FormatoContenido.PODCAST, "https://a.mp3", null,
                EnumSet.of(TipoRecursoAccesible.TRANSCRIPCION));
        assertTrue(lista.isEmpty());
    }

    @Test
    void elAudioDeApoyoNoReemplazaLaTranscripcion() {
        List<Hallazgo> lista = AccesibilidadContenidoService.analizar(FormatoContenido.PODCAST, "https://a.mp3", null,
                EnumSet.of(TipoRecursoAccesible.AUDIO, TipoRecursoAccesible.SUBTITULO));
        assertTrue(lista.stream().anyMatch(x -> x.regla().equals("AUDIO_SIN_TRANSCRIPCION")));
    }

    @Test
    void laReglaDeConformidadDeRf019TambienExigeLaTranscripcion() {
        Contenido c = new Contenido();
        c.setFormato(FormatoContenido.PODCAST);
        c.setUrlRecurso("https://a.mp3");

        assertTrue(ConformidadAccesibilidad.faltantes(c, List.of()).stream().anyMatch(f -> f.contains("transcripción")));

        RecursoAccesible t = new RecursoAccesible();
        t.setTipo(TipoRecursoAccesible.TRANSCRIPCION);
        t.setDisponible(true);
        assertTrue(ConformidadAccesibilidad.faltantes(c, List.of(t)).isEmpty());
    }

    @Test
    void laVerificacionTardaMenosDeDosSegundos() {
        long inicio = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            AccesibilidadContenidoService.analizar(FormatoContenido.PODCAST, "https://a.mp3", null,
                    EnumSet.noneOf(TipoRecursoAccesible.class));
        }
        assertTrue((System.nanoTime() - inicio) / 1_000_000 < 2000);
    }
}
