package com.elearning.platform.dto.response;

import com.elearning.platform.enums.ContentType;
import com.elearning.platform.enums.LearningStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {

    private Long id;

    private String title;

    private String description;

    private String url;

    private ContentType type;

    private LearningStyle learningStyle;

    private Integer durationMinutes;

    private Double completionPercentage;
    
    private Boolean completed;
}