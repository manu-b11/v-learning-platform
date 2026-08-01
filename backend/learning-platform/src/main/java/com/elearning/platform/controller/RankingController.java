package com.elearning.platform.controller;

import com.elearning.platform.dto.response.RankingResponse;
import com.elearning.platform.services.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    // Obtener ranking general
    @GetMapping
    public List<RankingResponse> getRanking() {

        return rankingService.getRanking();
    }

}