package com.elearning.platform.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResultResponse {

    private Long id;

    private Long evaluationId;

    private String evaluationTitle;

    private Double score;

    private Boolean completed;

}