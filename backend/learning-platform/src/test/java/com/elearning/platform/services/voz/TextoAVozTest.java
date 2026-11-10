package com.elearning.platform.services.voz;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RF-036: implementaciones de ServicioTextoAVoz. */
class TextoAVozTest {

    @Test
    void laImplementacionDePruebaDevuelveTramasMp3Validas() {
        byte[] mp3 = new TextoAVozPrueba().sintetizar("Hola", 1);
        assertEquals(TextoAVozPrueba.TRAMAS_POR_SEGUNDO * TextoAVozPrueba.BYTES_TRAMA, mp3.length);
        for (int i = 0; i < mp3.length; i += TextoAVozPrueba.BYTES_TRAMA) {
            assertEquals((byte) 0xFF, mp3[i], "sincronía de trama");
            assertEquals((byte) 0xFB, mp3[i + 1], "MPEG-1 capa III");
        }
    }

    @Test
    void espeakRecibeLaVelocidadYElTextoPorLaEntradaEstandar() {
        List<String> cmd = TextoAVozEspeak.comandoEspeak("espeak-ng", "es-419", 1.25);
        assertEquals(List.of("espeak-ng", "-v", "es-419", "-s", "200", "--stdin", "--stdout"), cmd);
    }

    @Test
    void ffmpegConvierteDeWavAMp3PorTuberias() {
        List<String> cmd = TextoAVozEspeak.comandoFfmpeg("ffmpeg");
        assertTrue(cmd.containsAll(List.of("pipe:0", "pipe:1", "libmp3lame", "mp3")));
    }
}
