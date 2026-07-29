package com.elearning.platform.dto.response;

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
public class ProgressResponse {

    private Long id;

    private Long contentId;

    private String contentTitle;

    private Long moduleId;

    private String moduleTitle;

    private Double completionPercentage;

    private Boolean completed;

}