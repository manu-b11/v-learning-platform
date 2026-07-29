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
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // Actualizar progreso de un contenido
    @PutMapping("/api/contents/{contentId}/progress")
    public ResponseEntity<ProgressResponse> updateProgress(
            @PathVariable Long contentId,
            @Valid @RequestBody UpdateProgressRequest request
    ) {

        ProgressResponse progress =
                progressService.updateProgress(contentId, request);

        return ResponseEntity.ok(progress);
    }

    // Obtener progreso del usuario autenticado
    @GetMapping("/api/progress/me")
    public ResponseEntity<List<ProgressResponse>> getMyProgress() {

        List<ProgressResponse> progress =
                progressService.getMyProgress();

        return ResponseEntity.ok(progress);
    }

}