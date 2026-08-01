package com.elearning.platform.controller;

import com.elearning.platform.dto.request.CreateBadgeRequest;
import com.elearning.platform.dto.request.UpdateBadgeRequest;
import com.elearning.platform.dto.response.BadgeResponse;
import com.elearning.platform.dto.response.UserBadgeResponse;
import com.elearning.platform.services.BadgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    // Crear insignia
    @PostMapping
    public BadgeResponse createBadge(
            @Valid @RequestBody CreateBadgeRequest request
    ) {

        return badgeService.createBadge(request);
    }

    // Obtener todas las insignias
    @GetMapping
    public List<BadgeResponse> getAllBadges() {

        return badgeService.getAllBadges();
    }

    // Obtener insignia por id
    @GetMapping("/{id}")
    public BadgeResponse getBadgeById(
            @PathVariable Long id
    ) {

        return badgeService.getBadgeById(id);
    }

    // Actualizar insignia
    @PutMapping("/{id}")
    public BadgeResponse updateBadge(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBadgeRequest request
    ) {

        return badgeService.updateBadge(id, request);
    }

    // Eliminar insignia
    @DeleteMapping("/{id}")
    public void deleteBadge(
            @PathVariable Long id
    ) {

        badgeService.deleteBadge(id);
    }

    // Obtener mis insignias
    @GetMapping("/me")
    public List<UserBadgeResponse> getMyBadges() {

        return badgeService.getMyBadges();
    }

}