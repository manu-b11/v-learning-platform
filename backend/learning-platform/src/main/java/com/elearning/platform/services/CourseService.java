package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateCourseRequest;
import com.elearning.platform.dto.request.UpdateCourseRequest;
import com.elearning.platform.dto.response.CourseCardResponse;
import com.elearning.platform.dto.response.CourseDetailResponse;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.User;
import com.elearning.platform.entity.VarkResult;
import com.elearning.platform.enums.LearningStyle;
import com.elearning.platform.repository.CourseRepository;
import com.elearning.platform.repository.UserRepository;
import com.elearning.platform.repository.VarkResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final VarkResultRepository varkResultRepository;

    // Crear curso
    public CourseDetailResponse createCourse(CreateCourseRequest request) {

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .learningStyle(request.getLearningStyle())
                .build();

        courseRepository.save(course);

        return buildDetailResponse(course);
    }

    // Obtener cursos del usuario según su estilo VARK
    public List<CourseCardResponse> getAllCourses() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        VarkResult result = varkResultRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Aún no has realizado el test VARK")
                );

        LearningStyle learningStyle = result.getDominantStyle();

        return courseRepository
                .findByLearningStyleAndActiveTrue(learningStyle)
                .stream()
                .map(this::buildCardResponse)
                .toList();
    }

    // Obtener curso por id
    public CourseDetailResponse getCourseById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        return buildDetailResponse(course);
    }

    // Actualizar curso
    public CourseDetailResponse updateCourse(
            Long id,
            UpdateCourseRequest request
    ) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setActive(request.getActive());
        course.setLearningStyle(request.getLearningStyle());

        courseRepository.save(course);

        return buildDetailResponse(course);
    }

    // Eliminar curso
    public void deleteCourse(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        courseRepository.delete(course);
    }

    // Construir respuesta para las cards
    private CourseCardResponse buildCardResponse(Course course) {

        return CourseCardResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .imageUrl(course.getImageUrl())
                .learningStyle(course.getLearningStyle())
                .progress(0)
                .remainingMinutes(0)
                .build();
    }

    // Construir respuesta para el detalle
    private CourseDetailResponse buildDetailResponse(Course course) {

        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .imageUrl(course.getImageUrl())
                .learningStyle(course.getLearningStyle())
                .progress(0)
                .remainingMinutes(0)
                .build();
    }

}