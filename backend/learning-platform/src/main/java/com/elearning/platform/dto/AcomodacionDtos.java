package com.elearning.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** RF-041: DTOs de acomodaciones de evaluación por estudiante. */
public final class AcomodacionDtos {

    private AcomodacionDtos() {}

    /** multiplicadorTiempo admite 1.5 o 2 (o null). Reemplaza la acomodación activa del estudiante. */
    public record AcomodacionRequest(
            @NotNull(message = "Elige el estudiante.") Long estudianteId,
            BigDecimal multiplicadorTiempo,
            Boolean tiempoIlimitado,
            @Min(value = 0, message = "Mínimo 0.") @Max(value = 5, message = "Máximo 5 intentos extra.") Integer intentosExtra,
            LocalDateTime fechaLimiteEspecial,
            @Min(value = 0, message = "Mínimo 0.") @Max(value = 60, message = "Máximo 60 minutos.") Integer minutosAmpliacion,
            LocalDate vigenteHasta) {}

    public record AcomodacionVista(
            Long id,
            Long estudianteId,
            String estudiante,
            BigDecimal multiplicadorTiempo,
            boolean tiempoIlimitado,
            int intentosExtra,
            LocalDateTime fechaLimiteEspecial,
            int minutosAmpliacion,
            LocalDate vigenteHasta,
            boolean vencida,
            Long creadaPor,
            LocalDateTime fechaCreacion) {}
}
