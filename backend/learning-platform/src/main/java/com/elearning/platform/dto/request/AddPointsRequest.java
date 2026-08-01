package com.elearning.platform.dto.request;

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
public class AddPointsRequest {

    @NotNull(message = "Los puntos son obligatorios")
    @Min(value = 1, message = "Debe agregar al menos 1 punto")
    private Integer points;

}