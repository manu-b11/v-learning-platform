package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateContentRequest;
import com.elearning.platform.dto.request.UpdateContentRequest;
import com.elearning.platform.dto.response.ContentResponse;
import com.elearning.platform.entity.Content;
import com.elearning.platform.entity.Module;
import com.elearning.platform.entity.User;
import com.elearning.platform.exception.ResourceNotFoundException;
import com.elearning.platform.mapper.ContentMapper;
import com.elearning.platform.repository.ContentRepository;
import com.elearning.platform.repository.ModuleRepository;
import com.elearning.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final ContentMapper contentMapper;

    // Crear contenido
    public ContentResponse createContent(
            Long moduleId,
            CreateContentRequest request
    ) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Módulo no encontrado")
                );

        Content content = Content.builder()
                .title(request.getTitle())
                .url(request.getUrl())
                .description(request.getDescription())
                .type(request.getType())
                .learningStyle(request.getLearningStyle())
                .durationMinutes(request.getDurationMinutes())
                .module(module)
                .build();

        Content savedContent = contentRepository.save(content);

        return contentMapper.toResponse(
                savedContent,
                getAuthenticatedUser()
        );
    }

    // Obtener contenidos de un módulo
    public List<ContentResponse> getContentsByModule(Long moduleId) {

        User user = getAuthenticatedUser();

        return contentRepository.findByModuleId(moduleId)
                .stream()
                .map(content -> contentMapper.toResponse(content, user))
                .toList();
    }

    // Obtener contenido por id
    public ContentResponse getContentById(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contenido no encontrado")
                );

        return contentMapper.toResponse(
                content,
                getAuthenticatedUser()
        );
    }

    // Actualizar contenido
    public ContentResponse updateContent(
            Long id,
            UpdateContentRequest request
    ) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contenido no encontrado")
                );

        content.setTitle(request.getTitle());
        content.setUrl(request.getUrl());
        content.setDescription(request.getDescription());
        content.setType(request.getType());
        content.setLearningStyle(request.getLearningStyle());
        content.setDurationMinutes(request.getDurationMinutes());

        Content savedContent = contentRepository.save(content);

        return contentMapper.toResponse(
                savedContent,
                getAuthenticatedUser()
        );
    }

    // Eliminar contenido
    public void deleteContent(Long id) {

        Content content = contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contenido no encontrado")
                );

        contentRepository.delete(content);
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

}