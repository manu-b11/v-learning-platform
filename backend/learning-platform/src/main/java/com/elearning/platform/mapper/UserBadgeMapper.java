package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.UserBadgeResponse;
import com.elearning.platform.entity.UserBadge;
import org.springframework.stereotype.Component;

@Component
public class UserBadgeMapper {

    public UserBadgeResponse toResponse(UserBadge userBadge) {

        return UserBadgeResponse.builder()
                .id(userBadge.getId())
                .badgeId(userBadge.getBadge().getId())
                .badgeName(userBadge.getBadge().getName())
                .badgeDescription(userBadge.getBadge().getDescription())
                .badgeIcon(userBadge.getBadge().getIcon())
                .requiredPoints(userBadge.getBadge().getRequiredPoints())
                .unlockedAt(userBadge.getUnlockedAt())
                .build();
    }

}