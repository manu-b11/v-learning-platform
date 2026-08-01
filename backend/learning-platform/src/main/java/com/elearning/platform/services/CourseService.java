package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateCourseRequest;
import com.elearning.platform.dto.request.UpdateCourseRequest;
import com.elearning.platform.dto.response.CourseCardResponse;
import com.elearning.platform.dto.response.CourseDetailResponse;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.User;
import com.elearning.platform.entity.VarkResult;
import com.elearning.platform.enums.LearningStyle;
import com.elearning.platform.exception.ResourceNotFoundException;
import com.elearning.platform.mapper.CourseMapper;
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
    private final CourseMapper courseMapper;

    // Crear curso
    public CourseDetailResponse createCourse(CreateCourseRequest request) {

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null
                        ? request.getActive()
                        : true)
                .learningStyle(request.getLearningStyle())
                .build();

        Course savedCourse = courseRepository.save(course);

        return courseMapper.toDetailResponse(
                savedCourse,
                getAuthenticatedUser()
        );
    }

    // Obtener cursos según el estilo VARK del usuario
    public List<CourseCardResponse> getAllCourses() {

        User user = getAuthenticatedUser();

        VarkResult result = varkResultRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Aún no has realizado el test VARK"
                        )
                );

        LearningStyle learningStyle = result.getDominantStyle();

        return courseRepository
                .findByLearningStyleAndActiveTrue(learningStyle)
                .stream()
                .map(course -> courseMapper.toCardResponse(course, user))
                .toList();
    }

    // Obtener curso por id
    public CourseDetailResponse getCourseById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Curso no encontrado")
                );

        return courseMapper.toDetailResponse(
                course,
                getAuthenticatedUser()
        );
    }

    // Actualizar curso
    public CourseDetailResponse updateCourse(
            Long id,
            UpdateCourseRequest request
    ) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Curso no encontrado")
                );

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setImageUrl(request.getImageUrl());
        course.setActive(request.getActive());
        course.setLearningStyle(request.getLearningStyle());

        Course savedCourse = courseRepository.save(course);

        return courseMapper.toDetailResponse(
                savedCourse,
                getAuthenticatedUser()
        );
    }

    // Eliminar curso
    public void deleteCourse(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Curso no encontrado")
                );

        courseRepository.delete(course);
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