package com.elearning.platform.services;

import com.elearning.platform.dto.request.UpdateProgressRequest;
import com.elearning.platform.dto.response.ProgressResponse;
import com.elearning.platform.entity.Module;
import com.elearning.platform.entity.Progress;
import com.elearning.platform.entity.User;
import com.elearning.platform.repository.ModuleRepository;
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
    private final ModuleRepository moduleRepository;

    // Actualizar progreso del módulo
    public ProgressResponse updateProgress(
            Long moduleId,
            UpdateProgressRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        Progress progress = progressRepository
                .findByUserIdAndModuleId(user.getId(), moduleId)
                .orElse(
                        Progress.builder()
                                .user(user)
                                .module(module)
                                .build()
                );

        progress.setCompletionPercentage(
                request.getCompletionPercentage()
        );
        progress.setScore(request.getScore());
        progress.setCompleted(request.getCompleted());

        progressRepository.save(progress);

        // Construir respuesta
        return buildResponse(progress);
    }

    // Obtener progreso del usuario
    public List<ProgressResponse> getMyProgress() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        return progressRepository.findByUserId(user.getId())
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Construir respuesta
    private ProgressResponse buildResponse(Progress progress) {

        return ProgressResponse.builder()
                .id(progress.getId())
                .moduleId(progress.getModule().getId())
                .moduleTitle(progress.getModule().getTitle())
                .completionPercentage(progress.getCompletionPercentage())
                .score(progress.getScore())
                .completed(progress.getCompleted())
                .build();
    }

}