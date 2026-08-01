package com.elearning.platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class CreateBadgeRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "El ícono es obligatorio")
    private String icon;

    @NotNull(message = "Los puntos requeridos son obligatorios")
    @Min(value = 0, message = "Los puntos requeridos no pueden ser negativos")
    private Integer requiredPoints;

}