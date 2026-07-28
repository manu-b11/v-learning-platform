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

public class CourseDetailResponse {

  private Long id;

private String title;

private String description;

private String imageUrl;

private LearningStyle learningStyle;

private Integer progress;

private Integer remainingMinutes;

}