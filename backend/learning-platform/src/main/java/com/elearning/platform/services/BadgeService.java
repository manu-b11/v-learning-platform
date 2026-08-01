package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateBadgeRequest;
import com.elearning.platform.dto.request.UpdateBadgeRequest;
import com.elearning.platform.dto.response.BadgeResponse;
import com.elearning.platform.dto.response.UserBadgeResponse;
import com.elearning.platform.entity.Badge;
import com.elearning.platform.entity.User;
import com.elearning.platform.entity.UserBadge;
import com.elearning.platform.entity.UserPoint;
import com.elearning.platform.mapper.BadgeMapper;
import com.elearning.platform.mapper.UserBadgeMapper;
import com.elearning.platform.repository.BadgeRepository;
import com.elearning.platform.repository.UserBadgeRepository;
import com.elearning.platform.repository.UserPointRepository;
import com.elearning.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserPointRepository userPointRepository;
    private final UserRepository userRepository;
    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;

    // Crear badge
    public BadgeResponse createBadge(CreateBadgeRequest request) {

        Badge badge = Badge.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .requiredPoints(request.getRequiredPoints())
                .build();

        Badge savedBadge = badgeRepository.save(badge);

        return badgeMapper.toResponse(savedBadge);
    }

    // Obtener todos los badges
    public List<BadgeResponse> getAllBadges() {

        return badgeRepository.findAllByOrderByRequiredPointsAsc()
                .stream()
                .map(badgeMapper::toResponse)
                .toList();
    }

    // Obtener badge por id
    public BadgeResponse getBadgeById(Long id) {

        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Insignia no encontrada")
                );

        return badgeMapper.toResponse(badge);
    }

    // Actualizar badge
    public BadgeResponse updateBadge(
            Long id,
            UpdateBadgeRequest request
    ) {

        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Insignia no encontrada")
                );

        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIcon(request.getIcon());
        badge.setRequiredPoints(request.getRequiredPoints());

        Badge savedBadge = badgeRepository.save(badge);

        return badgeMapper.toResponse(savedBadge);
    }

    // Eliminar badge
    public void deleteBadge(Long id) {

        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Insignia no encontrada")
                );

        badgeRepository.delete(badge);
    }

    // Obtener mis insignias
    public List<UserBadgeResponse> getMyBadges() {

        User user = getAuthenticatedUser();

        return userBadgeRepository.findByUserId(user.getId())
                .stream()
                .map(userBadgeMapper::toResponse)
                .toList();
    }

    // Verificar si el usuario desbloqueó nuevas insignias
    public void checkBadges(User user) {

        UserPoint userPoint = userPointRepository
                .findByUserId(user.getId())
                .orElse(null);

        if (userPoint == null) {
            return;
        }

        Integer totalPoints = userPoint.getTotalPoints();

        List<Badge> badges = badgeRepository
                .findAllByOrderByRequiredPointsAsc();

        for (Badge badge : badges) {

            boolean alreadyUnlocked =
                    userBadgeRepository.existsByUserIdAndBadgeId(
                            user.getId(),
                            badge.getId()
                    );

            if (!alreadyUnlocked &&
                    totalPoints >= badge.getRequiredPoints()) {

                UserBadge userBadge = UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .build();

                userBadgeRepository.save(userBadge);
            }
        }
    }

    // Obtener usuario autenticado
    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );
    }

}