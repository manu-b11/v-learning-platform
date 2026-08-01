package com.elearning.platform.controller;

import com.elearning.platform.dto.response.UserPointResponse;
import com.elearning.platform.services.UserPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class UserPointController {

    private final UserPointService userPointService;

    @GetMapping("/me")
    public UserPointResponse getMyPoints() {

        return userPointService.getMyPoints();
    }

}