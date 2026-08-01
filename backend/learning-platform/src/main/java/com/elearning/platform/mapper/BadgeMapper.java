package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.BadgeResponse;
import com.elearning.platform.entity.Badge;
import org.springframework.stereotype.Component;

@Component
public class BadgeMapper {

    public BadgeResponse toResponse(Badge badge) {

        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .icon(badge.getIcon())
                .requiredPoints(badge.getRequiredPoints())
                .build();
    }

}