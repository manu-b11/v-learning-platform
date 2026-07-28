package com.elearning.platform.dto.response;

import com.elearning.platform.enums.LearningStyle;
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
public class VarkResultResponse {

    private Long id;

    private Integer visualScore;

    private Integer auditoryScore;

    private Integer readingWritingScore;

    private Integer kinestheticScore;

    private LearningStyle dominantStyle;

}