package com.elearning.platform.services;

import com.elearning.platform.dto.response.RankingResponse;
import com.elearning.platform.entity.UserPoint;
import com.elearning.platform.mapper.RankingMapper;
import com.elearning.platform.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserPointRepository userPointRepository;
    private final RankingMapper rankingMapper;

    // Obtener ranking general
    public List<RankingResponse> getRanking() {

        List<UserPoint> ranking =
                userPointRepository.findAllByOrderByTotalPointsDesc();

        List<RankingResponse> response = new ArrayList<>();

        int position = 1;

        for (UserPoint userPoint : ranking) {

            response.add(
                    rankingMapper.toResponse(
                            userPoint,
                            position++
                    )
            );
        }

        return response;
    }

}