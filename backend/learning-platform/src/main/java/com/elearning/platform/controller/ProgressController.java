package com.elearning.platform.controller;

import com.elearning.platform.dto.request.UpdateProgressRequest;
import com.elearning.platform.dto.response.ProgressResponse;
import com.elearning.platform.services.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // Actualizar progreso del módulo
    @PutMapping("/modules/{moduleId}")
    public ResponseEntity<ProgressResponse> updateProgress(
            @PathVariable Long moduleId,
            @Valid @RequestBody UpdateProgressRequest request
    ) {

        ProgressResponse progress =
                progressService.updateProgress(moduleId, request);

        return ResponseEntity.ok(progress);
    }

    // Obtener progreso del usuario
    @GetMapping("/me")
    public ResponseEntity<List<ProgressResponse>> getMyProgress() {

        List<ProgressResponse> progress =
                progressService.getMyProgress();

        return ResponseEntity.ok(progress);
    }

}