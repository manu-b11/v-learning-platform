package com.elearning.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** RF-037: DTOs del borrador de subtítulos. */
public final class SubtitulosDtos {

    private SubtitulosDtos() {}

    /** Una línea con su tiempo en milisegundos. */
    public record LineaDto(@NotNull Long inicioMs, @NotNull Long finMs,
                           @Size(max = 500, message = "Máximo 500 caracteres por línea.") String texto) {}

    /** estado: SIN_BORRADOR, PENDIENTE, GENERANDO, LISTO, FALLIDO o APROBADO. */
    public record BorradorVista(Long contenidoId, String estado, String origen, String error, List<LineaDto> lineas,
                                boolean medioCambio, LocalDateTime fechaGeneracion, LocalDateTime fechaAprobacion) {}

    public record EdicionRequest(@NotNull @Size(max = 2000, message = "Máximo 2000 líneas.") List<@Valid LineaDto> lineas) {}
}
