package com.elearning.platform.controller;

import com.elearning.platform.dto.request.CreateModuleRequest;
import com.elearning.platform.dto.request.UpdateModuleRequest;
import com.elearning.platform.dto.response.ModuleResponse;
import com.elearning.platform.services.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // Crear módulo
    @PostMapping("/api/courses/{courseId}/modules")
    public ResponseEntity<ModuleResponse> createModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request
    ) {

        ModuleResponse module = moduleService.createModule(courseId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(module);
    }

    // Obtener módulos de un curso
    @GetMapping("/api/courses/{courseId}/modules")
    public ResponseEntity<List<ModuleResponse>> getModulesByCourse(
            @PathVariable Long courseId
    ) {

        List<ModuleResponse> modules =
                moduleService.getModulesByCourse(courseId);

        return ResponseEntity.ok(modules);
    }

    // Obtener módulo por id
    @GetMapping("/api/modules/{id}")
    public ResponseEntity<ModuleResponse> getModuleById(
            @PathVariable Long id
    ) {

        ModuleResponse module = moduleService.getModuleById(id);

        return ResponseEntity.ok(module);
    }

    // Actualizar módulo
    @PutMapping("/api/modules/{id}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModuleRequest request
    ) {

        ModuleResponse module =
                moduleService.updateModule(id, request);

        return ResponseEntity.ok(module);
    }

    // Eliminar módulo
    @DeleteMapping("/api/modules/{id}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long id
    ) {

        moduleService.deleteModule(id);

        return ResponseEntity.noContent().build();
    }

}