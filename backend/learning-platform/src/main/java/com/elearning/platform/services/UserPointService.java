package com.elearning.platform.services;

import com.elearning.platform.dto.response.UserPointResponse;
import com.elearning.platform.entity.User;
import com.elearning.platform.entity.UserPoint;
import com.elearning.platform.mapper.UserPointMapper;
import com.elearning.platform.repository.UserPointRepository;
import com.elearning.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointRepository userPointRepository;
    private final UserRepository userRepository;
    private final UserPointMapper userPointMapper;

    // Obtener mis puntos
    public UserPointResponse getMyPoints() {

        User user = getAuthenticatedUser();

        UserPoint userPoint = userPointRepository
                .findByUserId(user.getId())
                .orElseGet(() ->
                        userPointRepository.save(
                                UserPoint.builder()
                                        .user(user)
                                        .build()
                        )
                );

        return userPointMapper.toResponse(userPoint);
    }

    // Agregar puntos
    public void addPoints(
            User user,
            Integer points
    ) {

        UserPoint userPoint = userPointRepository
                .findByUserId(user.getId())
                .orElseGet(() ->
                        UserPoint.builder()
                                .user(user)
                                .build()
                );

        userPoint.setTotalPoints(
                userPoint.getTotalPoints() + points
        );

        userPointRepository.save(userPoint);
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