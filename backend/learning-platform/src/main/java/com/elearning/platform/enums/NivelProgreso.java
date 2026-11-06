package com.elearning.platform.enums;

import java.math.BigDecimal;

/**
 * Filtro de informes por nivel de progreso (RF-012). Se calcula con el porcentaje completado de la inscripción.
 */
public enum NivelProgreso {
    SIN_INICIAR("Sin iniciar (0 %)"),
    BAJO("Bajo (1 % a 39 %)"),
    MEDIO("Medio (40 % a 79 %)"),
    ALTO("Alto (80 % a 99 %)"),
    COMPLETADO("Completado (100 %)");

    private static final BigDecimal CUARENTA = new BigDecimal("40");
    private static final BigDecimal OCHENTA = new BigDecimal("80");
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final String texto;

    NivelProgreso(String texto) {
        this.texto = texto;
    }

    public String texto() {
        return texto;
    }

    /** Nivel que corresponde a un porcentaje de avance (null se trata como 0 %). */
    public static NivelProgreso de(BigDecimal porcentaje) {
        BigDecimal p = porcentaje == null ? BigDecimal.ZERO : porcentaje;
        if (p.signum() <= 0) return SIN_INICIAR;
        if (p.compareTo(CIEN) >= 0) return COMPLETADO;
        if (p.compareTo(OCHENTA) >= 0) return ALTO;
        if (p.compareTo(CUARENTA) >= 0) return MEDIO;
        return BAJO;
    }
}
