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

    @NotNull
    @Min(0)
    @Max(100)
    private Double completionPercentage;

    @NotNull
    @Min(0)
    @Max(100)
    private Double score;

    @NotNull
    private Boolean completed;

}