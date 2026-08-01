package com.elearning.platform.services;

import com.elearning.platform.dto.request.SaveVarkResultRequest;
import com.elearning.platform.dto.response.VarkResultResponse;
import com.elearning.platform.entity.User;
import com.elearning.platform.entity.VarkResult;
import com.elearning.platform.enums.LearningStyle;
import com.elearning.platform.exception.ResourceNotFoundException;
import com.elearning.platform.repository.UserRepository;
import com.elearning.platform.repository.VarkResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VarkResultService {

    private final VarkResultRepository varkResultRepository;
    private final UserRepository userRepository;

    // Guardar resultado VARK
    public VarkResultResponse saveResult(
            SaveVarkResultRequest request
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        VarkResult result = varkResultRepository
                .findByUserId(user.getId())
                .orElse(
                        VarkResult.builder()
                                .user(user)
                                .build()
                );

        result.setVisualScore(request.getVisualScore());
        result.setAuditoryScore(request.getAuditoryScore());
        result.setReadingWritingScore(
                request.getReadingWritingScore()
        );
        result.setKinestheticScore(request.getKinestheticScore());

        result.setDominantStyle(
                calculateDominantStyle(request)
        );

        varkResultRepository.save(result);

        return buildResponse(result);
    }

    // Obtener resultado del usuario
    public VarkResultResponse getMyResult() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        VarkResult result = varkResultRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Aún no has realizado el test VARK")
                );

        return buildResponse(result);
    }

    // Calcular estilo dominante
    private LearningStyle calculateDominantStyle(
            SaveVarkResultRequest request
    ) {

        int visual = request.getVisualScore();
        int auditory = request.getAuditoryScore();
        int reading = request.getReadingWritingScore();
        int kinesthetic = request.getKinestheticScore();

        int max = Math.max(
                Math.max(visual, auditory),
                Math.max(reading, kinesthetic)
        );

        if (max == visual) {
            return LearningStyle.VISUAL;
        }

        if (max == auditory) {
            return LearningStyle.AUDITORY;
        }

        if (max == reading) {
            return LearningStyle.READING_WRITING;
        }

        return LearningStyle.KINESTHETIC;
    }

    // Construir respuesta
    private VarkResultResponse buildResponse(
            VarkResult result
    ) {

        return VarkResultResponse.builder()
                .id(result.getId())
                .visualScore(result.getVisualScore())
                .auditoryScore(result.getAuditoryScore())
                .readingWritingScore(result.getReadingWritingScore())
                .kinestheticScore(result.getKinestheticScore())
                .dominantStyle(result.getDominantStyle())
                .build();
    }

}