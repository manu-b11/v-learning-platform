package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.RankingResponse;
import com.elearning.platform.entity.UserPoint;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

    public RankingResponse toResponse(
            UserPoint userPoint,
            Integer position
    ) {

        return RankingResponse.builder()
                .position(position)
                .userId(userPoint.getUser().getId())
                .firstName(userPoint.getUser().getFirstName())
                .lastName(userPoint.getUser().getLastName())
                .totalPoints(userPoint.getTotalPoints())
                .build();
    }

}