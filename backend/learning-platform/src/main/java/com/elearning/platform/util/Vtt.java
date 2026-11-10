package com.elearning.platform.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RF-037: lectura y escritura de subtítulos WebVTT y SRT. Los borradores automáticos y los
 * archivos que sube el instructor se guardan como líneas con tiempo (inicio, fin, texto).
 */
public final class Vtt {

    /** Una línea de subtítulos, en milisegundos. */
    public record Linea(long inicioMs, long finMs, String texto) {}

    private static final Pattern TIEMPO = Pattern.compile("^(?:(\\d+):)?(\\d{1,2}):(\\d{2})[.,](\\d{1,3})$");

    private Vtt() {}

    /** Lee VTT o SRT (los dos usan bloques separados por línea en blanco con «inicio --> fin»). */
    public static List<Linea> leer(String texto) {
        List<Linea> lineas = new ArrayList<>();
        if (texto == null) return lineas;
        for (String bloque : texto.replace("\r\n", "\n").replace('\r', '\n').split("\\n\\s*\\n")) {
            String[] filas = bloque.strip().split("\\n");
            int i = 0;
            while (i < filas.length && !filas[i].contains("-->")) i++;
            if (i >= filas.length) continue;
            String[] partes = filas[i].split("-->");
            Long inicio = aMs(partes[0]);
            Long fin = aMs(partes[1].trim().split("\\s+")[0]);
            if (inicio == null || fin == null) continue;
            StringBuilder sb = new StringBuilder();
            for (int j = i + 1; j < filas.length; j++) sb.append(filas[j].strip()).append(' ');
            String contenido = sb.toString().replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
            if (!contenido.isEmpty()) lineas.add(new Linea(inicio, fin, contenido));
        }
        lineas.sort(Comparator.comparingLong(Linea::inicioMs));
        return lineas;
    }

    /** Errores de las líneas editadas: tiempos válidos, en orden y con texto. Vacío si están bien. */
    public static List<String> validar(List<Linea> lineas) {
        List<String> errores = new ArrayList<>();
        if (lineas == null || lineas.isEmpty()) {
            errores.add("Agrega al menos una línea de subtítulos.");
            return errores;
        }
        long anterior = -1;
        for (int i = 0; i < lineas.size(); i++) {
            Linea l = lineas.get(i);
            int n = i + 1;
            if (l.inicioMs() < 0 || l.finMs() <= l.inicioMs()) errores.add("Línea " + n + ": el fin debe ser posterior al inicio.");
            if (l.inicioMs() < anterior) errores.add("Línea " + n + ": empieza antes que la línea anterior.");
            if (l.texto() == null || l.texto().isBlank()) errores.add("Línea " + n + ": escribe el texto.");
            anterior = l.inicioMs();
        }
        return errores;
    }

    public static String aVtt(List<Linea> lineas) {
        StringBuilder sb = new StringBuilder("WEBVTT\n\n");
        for (int i = 0; i < lineas.size(); i++) {
            Linea l = lineas.get(i);
            sb.append(i + 1).append('\n').append(marca(l.inicioMs(), '.')).append(" --> ").append(marca(l.finMs(), '.'))
                    .append('\n').append(l.texto()).append("\n\n");
        }
        return sb.toString();
    }

    /** Texto corrido para la transcripción (RF-019, RF-039). */
    public static String aTexto(List<Linea> lineas) {
        StringBuilder sb = new StringBuilder();
        for (Linea l : lineas) sb.append(l.texto()).append('\n');
        return sb.toString().trim();
    }

    public static Long aMs(String marca) {
        Matcher m = TIEMPO.matcher(marca.trim());
        if (!m.matches()) return null;
        long h = m.group(1) == null ? 0 : Long.parseLong(m.group(1));
        long min = Long.parseLong(m.group(2));
        long s = Long.parseLong(m.group(3));
        String frac = (m.group(4) + "00").substring(0, 3);
        if (min > 59 || s > 59) return null;
        return ((h * 60 + min) * 60 + s) * 1000 + Long.parseLong(frac);
    }

    static String marca(long ms, char separador) {
        long h = ms / 3_600_000, m = (ms / 60_000) % 60, s = (ms / 1000) % 60, r = ms % 1000;
        return String.format(Locale.ROOT, "%02d:%02d:%02d%c%03d", h, m, s, separador, r);
    }
}
