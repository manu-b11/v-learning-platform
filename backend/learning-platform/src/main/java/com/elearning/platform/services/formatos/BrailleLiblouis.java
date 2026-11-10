package com.elearning.platform.services.formatos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RF-032: braille español con liblouis (lou_translate). Se activa con
 * vlearning.braille.proveedor=liblouis; la tabla se configura con vlearning.braille.tabla.
 * El texto entra por la entrada estándar.
 */
@Component
@ConditionalOnProperty(name = "vlearning.braille.proveedor", havingValue = "liblouis")
public class BrailleLiblouis implements ServicioBraille {

    @Value("${vlearning.braille.lou-ruta:lou_translate}")
    private String programa;

    @Value("${vlearning.braille.tabla:es-g1.ctb}")
    private String tabla;

    @Override
    public String aBrf(String texto) {
        try {
            Process p = new ProcessBuilder(comando(programa, tabla)).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            try (OutputStream in = p.getOutputStream()) {
                in.write(texto.getBytes(StandardCharsets.UTF_8));
            }
            String salida = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!p.waitFor(60, TimeUnit.SECONDS) || p.exitValue() != 0) throw new IllegalStateException("Falló lou_translate.");
            return salida;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo ejecutar lou_translate", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la conversión a braille.", e);
        }
    }

    @Override
    public String nombre() {
        return "liblouis";
    }

    static List<String> comando(String programa, String tabla) {
        return List.of(programa, "--forward", "unicode.dis," + tabla);
    }
}
