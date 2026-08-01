package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.UserPointResponse;
import com.elearning.platform.entity.UserPoint;
import org.springframework.stereotype.Component;

@Component
public class UserPointMapper {

    public UserPointResponse toResponse(UserPoint userPoint) {

        return UserPointResponse.builder()
                .userId(userPoint.getUser().getId())
                .userName(
                        userPoint.getUser().getFirstName()
                                + " "
                                + userPoint.getUser().getLastName()
                )
                .totalPoints(userPoint.getTotalPoints())
                .build();
    }

}