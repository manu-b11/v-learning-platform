package com.elearning.platform.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {

    private Long id;

    private Integer orderNumber;

    private String title;

    private String description;

    private Integer maxScore;

    private Long moduleId;

}