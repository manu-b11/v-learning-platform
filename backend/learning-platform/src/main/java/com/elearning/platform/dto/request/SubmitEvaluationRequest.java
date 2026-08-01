package com.elearning.platform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitEvaluationRequest {

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 0, message = "La calificación no puede ser menor a 0")
    @Max(value = 100, message = "La calificación no puede ser mayor a 100")
    private Double score;

}