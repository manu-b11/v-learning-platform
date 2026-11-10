package com.elearning.platform.services.transcripcion;

import com.elearning.platform.util.Vtt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RF-037: reconocimiento de voz local con whisper.cpp. ffmpeg descarga el medio y lo convierte a
 * WAV mono de 16 kHz; whisper-cli escribe un VTT en español. Se activa con
 * vlearning.transcripcion.proveedor=whisper y requiere ambos programas y un modelo en el servidor.
 */
@Component
@ConditionalOnProperty(name = "vlearning.transcripcion.proveedor", havingValue = "whisper")
public class TranscripcionWhisper implements ServicioTranscripcion {

    @Value("${vlearning.transcripcion.ffmpeg-ruta:ffmpeg}")
    private String ffmpeg;

    @Value("${vlearning.transcripcion.whisper-ruta:whisper-cli}")
    private String whisper;

    @Value("${vlearning.transcripcion.modelo:models/ggml-base.bin}")
    private String modelo;

    @Override
    public List<Vtt.Linea> transcribir(String urlMedio) {
        if (urlMedio == null || !(urlMedio.startsWith("https://") || urlMedio.startsWith("http://"))) {
            throw new IllegalArgumentException("Solo se pueden transcribir enlaces http(s).");
        }
        Path dir = null;
        try {
            dir = Files.createTempDirectory("vl-subtitulos");
            Path wav = dir.resolve("audio.wav");
            Path salida = dir.resolve("subtitulos");
            ejecutar(comandoFfmpeg(ffmpeg, urlMedio, wav.toString()));
            ejecutar(comandoWhisper(whisper, modelo, wav.toString(), salida.toString()));
            return Vtt.leer(Files.readString(Path.of(salida + ".vtt"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el borrador de subtítulos.", e);
        } finally {
            borrar(dir);
        }
    }

    @Override
    public String nombre() {
        return "whisper.cpp";
    }

    /** La URL va como un argumento propio (no se arma una línea de shell). */
    static List<String> comandoFfmpeg(String programa, String url, String wav) {
        return List.of(programa, "-hide_banner", "-loglevel", "error", "-y", "-i", url, "-vn", "-ac", "1", "-ar", "16000", wav);
    }

    static List<String> comandoWhisper(String programa, String modelo, String wav, String salidaSinExtension) {
        return List.of(programa, "-m", modelo, "-f", wav, "-l", "es", "-ovtt", "-of", salidaSinExtension);
    }

    private static void ejecutar(List<String> comando) throws IOException {
        Process p = new ProcessBuilder(comando).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        try {
            if (!p.waitFor(30, TimeUnit.MINUTES) || p.exitValue() != 0) {
                p.destroyForcibly();
                throw new IllegalStateException("Falló " + comando.get(0) + ".");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la generación de subtítulos.", e);
        }
    }

    private static void borrar(Path dir) {
        if (dir == null) return;
        try (var archivos = Files.walk(dir)) {
            archivos.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignorada) {
            // carpeta temporal: el sistema la limpia si queda algo
        }
    }
}
