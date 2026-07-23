package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateContentRequest;
import com.elearning.platform.dto.request.UpdateContentRequest;
import com.elearning.platform.dto.response.ContentResponse;
import com.elearning.platform.entity.Content;
import com.elearning.platform.entity.Module;
import com.elearning.platform.repository.ContentRepository;
import com.elearning.platform.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ModuleRepository moduleRepository;

    // Crear contenido
    public ContentResponse createContent(
            Long moduleId,
            CreateContentRequest request
    ) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        Content content = Content.builder()
                .title(request.getTitle())
                .url(request.getUrl())
                .type(request.getType())
                .learningStyle(request.getLearningStyle())
                .module(module)
                .build();

        contentRepository.save(content);

        // Construir respuesta
        return buildResponse(content);
    }

    // Obtener contenidos de un módulo
    public List<ContentResponse> getContentsByModule(Long moduleId) {

        return contentRepository.findByModuleId(moduleId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener contenido por id
    public ContentResponse getContentById(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Contenido no encontrado")
                );

        // Construir respuesta
        return buildResponse(content);
    }

    // Actualizar contenido
    public ContentResponse updateContent(
            Long id,
            UpdateContentRequest request
    ) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Contenido no encontrado")
                );

        content.setTitle(request.getTitle());
        content.setUrl(request.getUrl());
        content.setType(request.getType());
        content.setLearningStyle(request.getLearningStyle());

        contentRepository.save(content);

        // Construir respuesta
        return buildResponse(content);
    }

    // Eliminar contenido
    public void deleteContent(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Contenido no encontrado")
                );

        contentRepository.delete(content);
    }

    // Construir respuesta
    private ContentResponse buildResponse(Content content) {

        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .url(content.getUrl())
                .type(content.getType())
                .learningStyle(content.getLearningStyle())
                .moduleId(content.getModule().getId())
                .build();
    }

}