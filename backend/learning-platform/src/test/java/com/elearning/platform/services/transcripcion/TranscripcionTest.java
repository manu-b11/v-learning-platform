package com.elearning.platform.services.transcripcion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RF-037: implementaciones de ServicioTranscripcion. */
class TranscripcionTest {

    @Test
    void laPruebaDevuelveUnBorradorDeEjemploMarcadoComoTal() {
        var lineas = new TranscripcionPrueba().transcribir("https://x/clase.mp4");
        assertFalse(lineas.isEmpty());
        assertTrue(lineas.get(0).texto().contains("Borrador automático"));
        assertThrows(IllegalArgumentException.class, () -> new TranscripcionPrueba().transcribir(" "));
    }

    @Test
    void whisperRecibeElEnlaceComoArgumentoYGeneraVttEnEspanol() {
        List<String> ff = TranscripcionWhisper.comandoFfmpeg("ffmpeg", "https://x/a.mp4;rm -rf /", "a.wav");
        assertTrue(ff.contains("https://x/a.mp4;rm -rf /"), "la URL es un solo argumento, sin shell");
        assertTrue(ff.containsAll(List.of("-ac", "1", "-ar", "16000")));
        List<String> wh = TranscripcionWhisper.comandoWhisper("whisper-cli", "m.bin", "a.wav", "salida");
        assertTrue(wh.containsAll(List.of("-l", "es", "-ovtt", "-of", "salida")));
    }

    @Test
    void whisperSoloAceptaEnlacesHttp() {
        assertThrows(IllegalArgumentException.class, () -> new TranscripcionWhisper().transcribir("file:///etc/passwd"));
    }
}
