package com.elearning.platform.controller;

import com.elearning.platform.dto.request.CreateContentRequest;
import com.elearning.platform.dto.request.UpdateContentRequest;
import com.elearning.platform.dto.response.ContentResponse;
import com.elearning.platform.services.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // Crear contenido
    @PostMapping("/api/modules/{moduleId}/contents")
    public ResponseEntity<ContentResponse> createContent(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateContentRequest request
    ) {

        ContentResponse content =
                contentService.createContent(moduleId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(content);
    }

    // Obtener contenidos de un módulo
    @GetMapping("/api/modules/{moduleId}/contents")
    public ResponseEntity<List<ContentResponse>> getContentsByModule(
            @PathVariable Long moduleId
    ) {

        List<ContentResponse> contents =
                contentService.getContentsByModule(moduleId);

        return ResponseEntity.ok(contents);
    }

    // Obtener contenido por id
    @GetMapping("/api/contents/{id}")
    public ResponseEntity<ContentResponse> getContentById(
            @PathVariable Long id
    ) {

        ContentResponse content =
                contentService.getContentById(id);

        return ResponseEntity.ok(content);
    }

    // Actualizar contenido
    @PutMapping("/api/contents/{id}")
    public ResponseEntity<ContentResponse> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentRequest request
    ) {

        ContentResponse content =
                contentService.updateContent(id, request);

        return ResponseEntity.ok(content);
    }

    // Eliminar contenido
    @DeleteMapping("/api/contents/{id}")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long id
    ) {

        contentService.deleteContent(id);

        return ResponseEntity.noContent().build();
    }

}