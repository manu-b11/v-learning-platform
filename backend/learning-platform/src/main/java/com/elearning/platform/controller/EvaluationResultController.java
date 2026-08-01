package com.elearning.platform.controller;

import com.elearning.platform.dto.request.SubmitEvaluationRequest;
import com.elearning.platform.dto.response.EvaluationResultResponse;
import com.elearning.platform.services.EvaluationResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation-results")
@RequiredArgsConstructor
public class EvaluationResultController {

    private final EvaluationResultService evaluationResultService;

    // Presentar evaluación
    @PostMapping("/{evaluationId}")
    public ResponseEntity<EvaluationResultResponse> submitEvaluation(
            @PathVariable Long evaluationId,
            @Valid @RequestBody SubmitEvaluationRequest request
    ) {

        EvaluationResultResponse result =
                evaluationResultService.submitEvaluation(
                        evaluationId,
                        request
                );

        return ResponseEntity.ok(result);
    }

    // Obtener resultados del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<List<EvaluationResultResponse>> getMyResults() {

        List<EvaluationResultResponse> results =
                evaluationResultService.getMyResults();

        return ResponseEntity.ok(results);
    }

}