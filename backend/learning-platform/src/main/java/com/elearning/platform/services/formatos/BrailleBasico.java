package com.elearning.platform.services.formatos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RF-032: braille de grado 1 en ASCII braille (el formato BRF de las líneas braille e
 * impresoras). Letras, números (con signo de número), mayúsculas (con signo de mayúscula) y la
 * puntuación básica. Las tildes se transcriben sin acento: para braille español completo (ONCE)
 * se usa la implementación con liblouis. Activa por defecto.
 */
@Component
@ConditionalOnProperty(name = "vlearning.braille.proveedor", havingValue = "basico", matchIfMissing = true)
public class BrailleBasico implements ServicioBraille {

    static final int CELDAS_POR_LINEA = 40;
    static final int LINEAS_POR_PAGINA = 25;

    /** Dígitos 1-9,0 se escriben con las letras a-j precedidas del signo de número «#». */
    private static final String DIGITOS = "jabcdefghi";
    private static final Map<Character, String> PUNTUACION = Map.ofEntries(
            Map.entry(',', "1"), Map.entry(';', "2"), Map.entry(':', "3"), Map.entry('.', "4"),
            Map.entry('!', "6"), Map.entry('?', "8"), Map.entry('¿', "8"), Map.entry('¡', "6"),
            Map.entry('(', "7"), Map.entry(')', "7"), Map.entry('-', "-"), Map.entry('\'', "'"),
            Map.entry('"', "8"), Map.entry('/', "/"), Map.entry('%', "#j0"));

    @Override
    public String aBrf(String texto) {
        List<String> lineas = new ArrayList<>();
        for (String parrafo : texto.split("\\R")) {
            if (parrafo.isBlank()) continue;
            StringBuilder actual = new StringBuilder();
            for (String palabra : parrafo.trim().split("\\s+")) {
                String b = palabra(palabra);
                while (b.length() > CELDAS_POR_LINEA) { // palabra más larga que la línea
                    if (!actual.isEmpty()) { lineas.add(actual.toString()); actual.setLength(0); }
                    lineas.add(b.substring(0, CELDAS_POR_LINEA - 1) + "-");
                    b = b.substring(CELDAS_POR_LINEA - 1);
                }
                if (!actual.isEmpty() && actual.length() + 1 + b.length() > CELDAS_POR_LINEA) {
                    lineas.add(actual.toString());
                    actual.setLength(0);
                }
                if (!actual.isEmpty()) actual.append(' ');
                actual.append(b);
            }
            if (!actual.isEmpty()) lineas.add(actual.toString());
        }
        StringBuilder brf = new StringBuilder();
        for (int i = 0; i < lineas.size(); i++) {
            if (i > 0 && i % LINEAS_POR_PAGINA == 0) brf.append('\f');
            brf.append(lineas.get(i)).append("\r\n");
        }
        return brf.toString();
    }

    @Override
    public String nombre() {
        return "braille grado 1 (básico)";
    }

    /** Una palabra en ASCII braille. */
    static String palabra(String palabra) {
        String sinTildes = Normalizer.normalize(palabra.replace('ñ', '\u0001').replace('Ñ', '\u0002'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('\u0001', 'ñ').replace('\u0002', 'Ñ');
        StringBuilder sb = new StringBuilder();
        boolean enNumero = false;
        for (char ch : sinTildes.toCharArray()) {
            if (Character.isDigit(ch)) {
                if (!enNumero) sb.append('#');
                sb.append(DIGITOS.charAt(ch - '0'));
                enNumero = true;
                continue;
            }
            enNumero = false;
            if (ch == 'ñ' || ch == 'Ñ') {
                if (ch == 'Ñ') sb.append(',');
                sb.append(']'); // ñ: puntos 1-2-4-5-6
            } else if (Character.isLetter(ch) && ch < 128) {
                if (Character.isUpperCase(ch)) sb.append(',');
                sb.append(Character.toLowerCase(ch));
            } else if (PUNTUACION.containsKey(ch)) {
                sb.append(PUNTUACION.get(ch));
            }
        }
        return sb.toString();
    }
}
