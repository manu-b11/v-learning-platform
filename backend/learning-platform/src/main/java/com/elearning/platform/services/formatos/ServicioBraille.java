package com.elearning.platform.services.formatos;

/**
 * RF-032: convierte texto plano en braille electrónico (BRF, ASCII braille de 40 celdas por
 * línea y 25 líneas por página). La implementación se elige con vlearning.braille.proveedor:
 * «basico» (por defecto, tabla de grado 1 incluida) o «liblouis» (lou_translate instalado, con
 * la tabla española que configure la institución).
 */
public interface ServicioBraille {

    String aBrf(String texto);

    String nombre();
}
