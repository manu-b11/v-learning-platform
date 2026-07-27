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
public class VarkResultResponse {

    private Long id;

    private Integer visualScore;

    private Integer auditoryScore;

    private Integer readingWritingScore;

    private Integer kinestheticScore;

    private String dominantStyle;

}