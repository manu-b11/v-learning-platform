package com.elearning.platform.services;

import com.elearning.platform.dto.request.SubmitEvaluationRequest;
import com.elearning.platform.dto.response.EvaluationResultResponse;
import com.elearning.platform.entity.Evaluation;
import com.elearning.platform.entity.EvaluationResult;
import com.elearning.platform.entity.User;
import com.elearning.platform.exception.ResourceNotFoundException;
import com.elearning.platform.repository.EvaluationRepository;
import com.elearning.platform.repository.EvaluationResultRepository;
import com.elearning.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationResultService {

    private static final int EVALUATION_POINTS = 30;
    private static final double PASSING_SCORE = 60.0;

    private final EvaluationResultRepository evaluationResultRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final UserPointService userPointService;

    // Presentar evaluación
    public EvaluationResultResponse submitEvaluation(
            Long evaluationId,
            SubmitEvaluationRequest request
    ) {

        User user = getAuthenticatedUser();

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evaluación no encontrada")
                );

        EvaluationResult result = evaluationResultRepository
                .findByUserIdAndEvaluationId(
                        user.getId(),
                        evaluationId
                )
                .orElse(
                        EvaluationResult.builder()
                                .user(user)
                                .evaluation(evaluation)
                                .build()
                );

        boolean wasApproved = Boolean.TRUE.equals(result.getApproved());

        result.setScore(request.getScore());
        result.setCompleted(true);
        result.setApproved(request.getScore() >= PASSING_SCORE);

        EvaluationResult savedResult =
                evaluationResultRepository.save(result);

        if (!wasApproved && Boolean.TRUE.equals(savedResult.getApproved())) {
            userPointService.addPoints(user, EVALUATION_POINTS);
        }

        return buildResponse(savedResult);
    }

    // Obtener resultados del usuario
    public List<EvaluationResultResponse> getMyResults() {

        User user = getAuthenticatedUser();

        return evaluationResultRepository.findByUserId(user.getId())
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener usuario autenticado
    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );
    }

    // Construir respuesta
    private EvaluationResultResponse buildResponse(
            EvaluationResult result
    ) {

        return EvaluationResultResponse.builder()
                .id(result.getId())
                .evaluationId(result.getEvaluation().getId())
                .evaluationTitle(result.getEvaluation().getTitle())
                .score(result.getScore())
                .completed(result.getCompleted())
                .build();
    }

}