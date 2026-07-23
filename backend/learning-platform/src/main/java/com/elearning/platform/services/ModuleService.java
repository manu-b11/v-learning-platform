package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateModuleRequest;
import com.elearning.platform.dto.request.UpdateModuleRequest;
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
    public ModuleResponse createModule(
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

        // Construir respuesta
        return buildResponse(module);
    }

    // Obtener módulos de un curso
    public List<ModuleResponse> getModulesByCourse(Long courseId) {

        return moduleRepository.findByCourseIdOrderByOrderNumberAsc(courseId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener módulo por id
    public ModuleResponse getModuleById(Long id) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        // Construir respuesta
        return buildResponse(module);
    }

    // Actualizar módulo
    public ModuleResponse updateModule(
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

        // Construir respuesta
        return buildResponse(module);
    }

    // Eliminar módulo
    public void deleteModule(Long id) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Módulo no encontrado")
                );

        moduleRepository.delete(module);
    }

    // Construir respuesta
    private ModuleResponse buildResponse(Module module) {

        return ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderNumber(module.getOrderNumber())
                .courseId(module.getCourse().getId())
                .build();
    }

}