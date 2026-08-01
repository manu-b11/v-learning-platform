package com.elearning.platform.controller;

import com.elearning.platform.dto.request.CreateEvaluationRequest;
import com.elearning.platform.dto.request.UpdateEvaluationRequest;
import com.elearning.platform.dto.response.EvaluationResponse;
import com.elearning.platform.services.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    // Crear evaluación
    @PostMapping("/api/modules/{moduleId}/evaluations")
    public ResponseEntity<EvaluationResponse> createEvaluation(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateEvaluationRequest request
    ) {

        EvaluationResponse evaluation =
                evaluationService.createEvaluation(moduleId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(evaluation);
    }

    // Obtener evaluaciones de un módulo
    @GetMapping("/api/modules/{moduleId}/evaluations")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByModule(
            @PathVariable Long moduleId
    ) {

        List<EvaluationResponse> evaluations =
                evaluationService.getEvaluationsByModule(moduleId);

        return ResponseEntity.ok(evaluations);
    }

    // Obtener evaluación por id
    @GetMapping("/api/evaluations/{id}")
    public ResponseEntity<EvaluationResponse> getEvaluationById(
            @PathVariable Long id
    ) {

        EvaluationResponse evaluation =
                evaluationService.getEvaluationById(id);

        return ResponseEntity.ok(evaluation);
    }

    // Actualizar evaluación
    @PutMapping("/api/evaluations/{id}")
    public ResponseEntity<EvaluationResponse> updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationRequest request
    ) {

        EvaluationResponse evaluation =
                evaluationService.updateEvaluation(id, request);

        return ResponseEntity.ok(evaluation);
    }

    // Eliminar evaluación
    @DeleteMapping("/api/evaluations/{id}")
    public ResponseEntity<Void> deleteEvaluation(
            @PathVariable Long id
    ) {

        evaluationService.deleteEvaluation(id);

        return ResponseEntity.noContent().build();
    }

}