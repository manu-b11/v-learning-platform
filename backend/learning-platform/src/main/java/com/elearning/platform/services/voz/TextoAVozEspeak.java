package com.elearning.platform.services.voz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RF-036: texto a voz local con espeak-ng (voz en español) y ffmpeg (WAV → MP3). Se activa con
 * vlearning.tts.proveedor=espeak; ambos programas deben estar instalados en el servidor.
 */
@Component
@ConditionalOnProperty(name = "vlearning.tts.proveedor", havingValue = "espeak")
public class TextoAVozEspeak implements ServicioTextoAVoz {

    /** Palabras por minuto de espeak-ng a velocidad 1. */
    static final int PALABRAS_POR_MINUTO = 160;

    @Value("${vlearning.tts.espeak-ruta:espeak-ng}")
    private String espeak;

    @Value("${vlearning.tts.ffmpeg-ruta:ffmpeg}")
    private String ffmpeg;

    @Value("${vlearning.tts.voz:es-419}")
    private String voz;

    @Override
    public byte[] sintetizar(String texto, double velocidad) {
        byte[] wav = ejecutar(comandoEspeak(espeak, voz, velocidad), texto.getBytes(StandardCharsets.UTF_8));
        return ejecutar(comandoFfmpeg(ffmpeg), wav);
    }

    @Override
    public String nombre() {
        return "espeak-ng";
    }

    /** El texto entra por la entrada estándar: nunca se arma una línea de comandos con él. */
    static List<String> comandoEspeak(String programa, String voz, double velocidad) {
        int ppm = (int) Math.round(PALABRAS_POR_MINUTO * velocidad);
        return List.of(programa, "-v", voz, "-s", String.valueOf(ppm), "--stdin", "--stdout");
    }

    static List<String> comandoFfmpeg(String programa) {
        return List.of(programa, "-hide_banner", "-loglevel", "error", "-f", "wav", "-i", "pipe:0",
                "-codec:a", "libmp3lame", "-q:a", "4", "-f", "mp3", "pipe:1");
    }

    private static byte[] ejecutar(List<String> comando, byte[] entrada) {
        try {
            Process p = new ProcessBuilder(comando).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            Thread escritor = Thread.ofVirtual().start(() -> {
                try (OutputStream in = p.getOutputStream()) {
                    in.write(entrada);
                } catch (IOException ignorada) {
                    // si el proceso termina antes, el código de salida lo reporta
                }
            });
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            try (InputStream out = p.getInputStream()) {
                out.transferTo(salida);
            }
            escritor.join();
            if (!p.waitFor(60, TimeUnit.SECONDS) || p.exitValue() != 0) {
                throw new IllegalStateException("Falló " + comando.get(0) + " (código " + p.exitValue() + ").");
            }
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo ejecutar " + comando.get(0), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la generación del audio.", e);
        }
    }
}
