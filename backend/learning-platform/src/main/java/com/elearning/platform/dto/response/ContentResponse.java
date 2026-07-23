package com.elearning.platform.dto.response;

import com.elearning.platform.enums.ContentType;
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
public class ContentResponse {

    private Long id;

    private String title;

    private String url;

    private ContentType type;

    private LearningStyle learningStyle;

    private Long moduleId;

}