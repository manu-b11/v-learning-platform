package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateCourseRequest;
import com.elearning.platform.dto.request.UpdateCourseRequest;
import com.elearning.platform.dto.response.CourseResponse;
import com.elearning.platform.entity.Course;
import com.elearning.platform.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // Crear curso
    public CourseResponse createCourse(CreateCourseRequest request) {

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        courseRepository.save(course);

        // Construir respuesta
        return buildResponse(course);
    }

    // Obtener todos los cursos
    public List<CourseResponse> getAllCourses() {

        return courseRepository.findAll()
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Obtener curso por id
    public CourseResponse getCourseById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        // Construir respuesta
        return buildResponse(course);
    }

    // Actualizar curso
    public CourseResponse updateCourse(
            Long id,
            UpdateCourseRequest request
    ) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setActive(request.getActive());

        courseRepository.save(course);

        // Construir respuesta
        return buildResponse(course);
    }

    // Eliminar curso
    public void deleteCourse(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        courseRepository.delete(course);
    }

    // Construir respuesta
    private CourseResponse buildResponse(Course course) {

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .active(course.getActive())
                .build();
    }

}