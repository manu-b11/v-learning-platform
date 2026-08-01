package com.elearning.platform.services;

import com.elearning.platform.dto.response.ProgressResponse;
import com.elearning.platform.dto.request.UpdateProgressRequest;
import com.elearning.platform.entity.Content;
import com.elearning.platform.entity.Progress;
import com.elearning.platform.entity.User;
import com.elearning.platform.repository.ContentRepository;
import com.elearning.platform.repository.ProgressRepository;
import com.elearning.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final UserPointService userPointService;
    private final BadgeService badgeService;

    // Actualizar progreso del contenido
    public ProgressResponse updateProgress(
            Long contentId,
            UpdateProgressRequest request
    ) {

        User user = getAuthenticatedUser();

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() ->
                        new RuntimeException("Contenido no encontrado")
                );

        Progress progress = progressRepository
                .findByUserIdAndContentId(user.getId(), contentId)
                .orElse(
                        Progress.builder()
                                .user(user)
                                .content(content)
                                .build()
                );

        // Saber si anteriormente ya estaba completado
        boolean wasCompleted =
                Boolean.TRUE.equals(progress.getCompleted());

        progress.setCompletionPercentage(
                request.getCompletionPercentage()
        );

        progress.setCompleted(
                request.getCompleted()
        );

        Progress savedProgress = progressRepository.save(progress);

        // Solo otorgar puntos la primera vez que completa el contenido
        if (!wasCompleted &&
                Boolean.TRUE.equals(savedProgress.getCompleted())) {

            userPointService.addPoints(
                    user,
                    10
            );

            badgeService.checkBadges(user);
        }

        return buildResponse(savedProgress);
    }

    // Obtener progreso del usuario
    public List<ProgressResponse> getMyProgress() {

        User user = getAuthenticatedUser();

        return progressRepository.findByUserId(user.getId())
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener usuario autenticado
    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        )
                );
    }

    // Construir respuesta
    private ProgressResponse buildResponse(
            Progress progress
    ) {

        return ProgressResponse.builder()
                .id(progress.getId())
                .contentId(progress.getContent().getId())
                .contentTitle(progress.getContent().getTitle())
                .moduleId(progress.getContent().getModule().getId())
                .moduleTitle(progress.getContent().getModule().getTitle())
                .completionPercentage(
                        progress.getCompletionPercentage()
                )
                .completed(progress.getCompleted())
                .build();
    }

}