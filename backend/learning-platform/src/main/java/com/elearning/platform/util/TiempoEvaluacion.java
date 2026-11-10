package com.elearning.platform.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * RF-041: condiciones de tiempo de un estudiante en un curso, calculadas siempre en el servidor.
 * Combina el tiempo adicional del perfil (RF-021, en %) con la acomodación vigente: se aplica el
 * MAYOR de los dos y no se suman. El tiempo ilimitado anula el límite.
 *
 * @param extraPerfilPorcentaje tiempo adicional del perfil de accesibilidad (0 a 200 %)
 * @param multiplicador         multiplicador de la acomodación (1,5 o 2) o null
 * @param ilimitado             la acomodación da tiempo ilimitado
 * @param fechaLimiteEspecial   fecha límite propia para iniciar, o null
 * @param minutosAmpliacion     RF-042: minutos que se pueden pedir una vez por intento (0 = no)
 */
public record TiempoEvaluacion(int extraPerfilPorcentaje, BigDecimal multiplicador, boolean ilimitado,
                               LocalDateTime fechaLimiteEspecial, int minutosAmpliacion) {

    /** Solo el tiempo adicional del perfil, sin acomodación. */
    public static TiempoEvaluacion soloPerfil(int extraPerfilPorcentaje) {
        return new TiempoEvaluacion(extraPerfilPorcentaje, null, false, null, 0);
    }

    /** Factor que se aplica al tiempo base: el mayor entre el perfil y la acomodación. */
    public BigDecimal factor() {
        BigDecimal perfil = BigDecimal.valueOf(100L + Math.max(0, extraPerfilPorcentaje))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        if (multiplicador == null) return perfil;
        return perfil.max(multiplicador);
    }

    /** Minutos totales para una evaluación; null si no tiene límite o la acomodación da tiempo ilimitado. */
    public Integer minutosLimite(Integer minutosBase) {
        if (minutosBase == null || ilimitado) return null;
        return BigDecimal.valueOf(minutosBase).multiply(factor()).setScale(0, RoundingMode.CEILING).intValueExact();
    }

    /** Último momento para INICIAR: la fecha de la evaluación o la especial, la que sea posterior. */
    public LocalDateTime fechaLimite(LocalDateTime fechaEvaluacion) {
        if (fechaEvaluacion == null || fechaLimiteEspecial == null) return fechaEvaluacion;
        return fechaLimiteEspecial.isAfter(fechaEvaluacion) ? fechaLimiteEspecial : fechaEvaluacion;
    }
}
