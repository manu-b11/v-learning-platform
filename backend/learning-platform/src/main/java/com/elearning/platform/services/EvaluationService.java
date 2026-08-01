package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateEvaluationRequest;
import com.elearning.platform.dto.request.UpdateEvaluationRequest;
import com.elearning.platform.dto.response.EvaluationResponse;
import com.elearning.platform.entity.Evaluation;
import com.elearning.platform.entity.Module;
import com.elearning.platform.exception.ResourceNotFoundException;
import com.elearning.platform.repository.EvaluationRepository;
import com.elearning.platform.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ModuleRepository moduleRepository;

    // Crear evaluación
    public EvaluationResponse createEvaluation(
            Long moduleId,
            CreateEvaluationRequest request
    ) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Módulo no encontrado")
                );

        Evaluation evaluation = Evaluation.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .maxScore(request.getMaxScore())
                .orderNumber(request.getOrderNumber())
                .module(module)
                .build();

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);

        return buildResponse(savedEvaluation);
    }

    // Obtener evaluaciones de un módulo
    public List<EvaluationResponse> getEvaluationsByModule(
            Long moduleId
    ) {

        return evaluationRepository
                .findByModuleIdOrderByOrderNumberAsc(moduleId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener evaluación por id
    public EvaluationResponse getEvaluationById(Long id) {

        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Evaluación no encontrada")
                );

        return buildResponse(evaluation);
    }

    // Actualizar evaluación
    public EvaluationResponse updateEvaluation(
            Long id,
            UpdateEvaluationRequest request
    ) {

        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Evaluación no encontrada")
                );

        evaluation.setTitle(request.getTitle());
        evaluation.setDescription(request.getDescription());
        evaluation.setMaxScore(request.getMaxScore());
        evaluation.setOrderNumber(request.getOrderNumber());

        Evaluation savedEvaluation =
                evaluationRepository.save(evaluation);

        return buildResponse(savedEvaluation);
    }

    // Eliminar evaluación
    public void deleteEvaluation(Long id) {

        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Evaluación no encontrada")
                );

        evaluationRepository.delete(evaluation);
    }

    // Construir respuesta
    private EvaluationResponse buildResponse(
            Evaluation evaluation
    ) {

        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .title(evaluation.getTitle())
                .description(evaluation.getDescription())
                .maxScore(evaluation.getMaxScore())
                .orderNumber(evaluation.getOrderNumber())
                .moduleId(evaluation.getModule().getId())
                .build();
    }

}