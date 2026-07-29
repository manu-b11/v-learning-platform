package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateModuleRequest;
import com.elearning.platform.dto.request.UpdateModuleRequest;
import com.elearning.platform.dto.response.ContentResponse;
import com.elearning.platform.dto.response.ModuleDetailResponse;
import com.elearning.platform.dto.response.ModuleResponse;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.Module;
import com.elearning.platform.repository.CourseRepository;
import com.elearning.platform.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    // Crear módulo
    public ModuleDetailResponse createModule(
            Long courseId,
            CreateModuleRequest request
    ) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderNumber(request.getOrderNumber())
                .course(course)
                .build();

        moduleRepository.save(module);

        return buildDetailResponse(module);
    }

    // Obtener módulos de un curso
    public List<ModuleResponse> getModulesByCourse(Long courseId) {

        return moduleRepository.findByCourseIdOrderByOrderNumberAsc(courseId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener módulo por id
    public ModuleDetailResponse getModuleById(Long id) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        return buildDetailResponse(module);
    }

    // Actualizar módulo
    public ModuleDetailResponse updateModule(
            Long id,
            UpdateModuleRequest request
    ) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setOrderNumber(request.getOrderNumber());

        moduleRepository.save(module);

        return buildDetailResponse(module);
    }

    // Eliminar módulo
    public void deleteModule(Long id) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        moduleRepository.delete(module);
    }

    // Construir respuesta para listado
    private ModuleResponse buildResponse(Module module) {

        return ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderNumber(module.getOrderNumber())
                .build();
    }

   // Construir respuesta para detalle
private ModuleDetailResponse buildDetailResponse(Module module) {

    return ModuleDetailResponse.builder()
            .id(module.getId())
            .title(module.getTitle())
            .description(module.getDescription())
            .orderNumber(module.getOrderNumber())
            .contents(
                    module.getContents()
                            .stream()
                            .map(content -> ContentResponse.builder()
                                    .id(content.getId())
                                    .title(content.getTitle())
                                    .url(content.getUrl())
                                    .description(content.getDescription())
                                    .type(content.getType())
                                    .learningStyle(content.getLearningStyle())
                                    .durationMinutes(content.getDurationMinutes())
                                    .build())
                            .toList()
            )
            .build();
}

}