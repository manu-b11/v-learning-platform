package com.elearning.platform.services;

import com.elearning.platform.dto.request.CreateCourseRequest;
import com.elearning.platform.dto.request.UpdateCourseRequest;
import com.elearning.platform.dto.response.CourseCardResponse;
import com.elearning.platform.dto.response.CourseDetailResponse;
import com.elearning.platform.entity.Content;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.Module;
import com.elearning.platform.entity.Progress;
import com.elearning.platform.entity.User;
import com.elearning.platform.entity.VarkResult;
import com.elearning.platform.enums.LearningStyle;
import com.elearning.platform.repository.CourseRepository;
import com.elearning.platform.repository.ProgressRepository;
import com.elearning.platform.repository.UserRepository;
import com.elearning.platform.repository.VarkResultRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final VarkResultRepository varkResultRepository;
    private final ProgressRepository progressRepository;

    @Getter
    @RequiredArgsConstructor
    private static class CourseProgressData {

        private final Integer progress;
        private final Integer remainingMinutes;
    }

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

        courseRepository.save(course);

        return buildDetailResponse(
                course,
                new CourseProgressData(0, 0)
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

        Map<Long, Progress> progressMap = progressRepository
                .findByUserId(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        progress -> progress.getContent().getId(),
                        Function.identity()
                ));

        return courseRepository
                .findByLearningStyleAndActiveTrue(learningStyle)
                .stream()
                .map(course ->
                        buildCardResponse(
                                course,
                                calculateCourseProgress(course, progressMap)
                        )
                )
                .toList();
    }

    // Obtener curso por id
    public CourseDetailResponse getCourseById(Long id) {

        User user = getAuthenticatedUser();

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        Map<Long, Progress> progressMap = progressRepository
                .findByUserId(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        progress -> progress.getContent().getId(),
                        Function.identity()
                ));

        return buildDetailResponse(
                course,
                calculateCourseProgress(course, progressMap)
        );
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

        User user = getAuthenticatedUser();

        Map<Long, Progress> progressMap = progressRepository
                .findByUserId(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        progress -> progress.getContent().getId(),
                        Function.identity()
                ));

        return buildDetailResponse(
                course,
                calculateCourseProgress(course, progressMap)
        );
    }

    // Eliminar curso
    public void deleteCourse(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
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
                        new RuntimeException("Usuario no encontrado")
                );
    }

    // Calcular progreso del curso
    private CourseProgressData calculateCourseProgress(
            Course course,
            Map<Long, Progress> progressMap
    ) {

        int totalContents = 0;
        int completedContents = 0;
        int remainingMinutes = 0;

        for (Module module : course.getModules()) {

            for (Content content : module.getContents()) {

                totalContents++;

                Progress progress = progressMap.get(content.getId());

                if (progress != null &&
                        Boolean.TRUE.equals(progress.getCompleted())) {

                    completedContents++;

                } else {

                    remainingMinutes += content.getDurationMinutes() != null
                            ? content.getDurationMinutes()
                            : 0;
                }
            }
        }

        int percentage = totalContents == 0
                ? 0
                : (completedContents * 100) / totalContents;

        return new CourseProgressData(
                percentage,
                remainingMinutes
        );
    }

    // Construir respuesta para las cards
    private CourseCardResponse buildCardResponse(
            Course course,
            CourseProgressData progressData
    ) {

        return CourseCardResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .imageUrl(course.getImageUrl())
                .learningStyle(course.getLearningStyle())
                .progress(progressData.getProgress())
                .remainingMinutes(progressData.getRemainingMinutes())
                .build();
    }

    // Construir respuesta para el detalle
    private CourseDetailResponse buildDetailResponse(
            Course course,
            CourseProgressData progressData
    ) {

        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .imageUrl(course.getImageUrl())
                .learningStyle(course.getLearningStyle())
                .progress(progressData.getProgress())
                .remainingMinutes(progressData.getRemainingMinutes())
                .build();
    }

}