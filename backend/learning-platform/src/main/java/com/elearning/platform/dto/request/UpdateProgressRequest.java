package com.elearning.platform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProgressRequest {

    @NotNull(message = "El porcentaje de progreso es obligatorio")
    @Min(value = 0, message = "El progreso no puede ser menor a 0")
    @Max(value = 100, message = "El progreso no puede ser mayor a 100")
    private Double completionPercentage;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 0, message = "La calificación no puede ser menor a 0")
    @Max(value = 100, message = "La calificación no puede ser mayor a 100")
    private Double score;

    @NotNull(message = "Debe indicar si el contenido está completado")
    private Boolean completed;

}