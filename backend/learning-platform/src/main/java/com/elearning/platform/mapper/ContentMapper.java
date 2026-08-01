package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.ContentResponse;
import com.elearning.platform.entity.Content;
import com.elearning.platform.entity.User;
import com.elearning.platform.services.ProgressCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentMapper {

    private final ProgressCalculatorService progressCalculatorService;

    public ContentResponse toResponse(
            Content content,
            User user
    ) {

        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .url(content.getUrl())
                .description(content.getDescription())
                .type(content.getType())
                .learningStyle(content.getLearningStyle())
                .durationMinutes(content.getDurationMinutes())
                .completionPercentage(
                        progressCalculatorService
                                .getContentCompletionPercentage(content, user)
                )
                .completed(
                        progressCalculatorService
                                .isContentCompleted(content, user)
                )
                .build();
    }

}