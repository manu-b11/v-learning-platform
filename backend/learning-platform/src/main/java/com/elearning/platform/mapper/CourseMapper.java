package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.CourseCardResponse;
import com.elearning.platform.dto.response.CourseDetailResponse;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.User;
import com.elearning.platform.services.ProgressCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final ProgressCalculatorService progressCalculatorService;

    public CourseCardResponse toCardResponse(
            Course course,
            User user
    ) {

        return CourseCardResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .imageUrl(course.getImageUrl())
                .learningStyle(course.getLearningStyle())
                .progress(
                        progressCalculatorService.getCourseProgress(
                                course,
                                user
                        )
                )
                .remainingMinutes(
                        progressCalculatorService.getRemainingMinutes(
                                course,
                                user
                        )
                )
                .build();
    }

    public CourseDetailResponse toDetailResponse(
            Course course,
            User user
    ) {

        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .imageUrl(course.getImageUrl())
                .learningStyle(course.getLearningStyle())
                .progress(
                        progressCalculatorService.getCourseProgress(
                                course,
                                user
                        )
                )
                .remainingMinutes(
                        progressCalculatorService.getRemainingMinutes(
                                course,
                                user
                        )
                )
                .build();
    }

}