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
public class SaveVarkResultRequest {

    @NotNull
    @Min(0)
    @Max(16)
    private Integer visualScore;

    @NotNull
    @Min(0)
    @Max(16)
    private Integer auditoryScore;

    @NotNull
    @Min(0)
    @Max(16)
    private Integer readingWritingScore;

    @NotNull
    @Min(0)
    @Max(16)
    private Integer kinestheticScore;

}