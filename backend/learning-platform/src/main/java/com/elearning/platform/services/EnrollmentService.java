package com.elearning.platform.services;

import com.elearning.platform.dto.response.EnrollmentResponse;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.Enrollment;
import com.elearning.platform.entity.User;
import com.elearning.platform.repository.CourseRepository;
import com.elearning.platform.repository.EnrollmentRepository;
import com.elearning.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    // Inscribir usuario en un curso
    public EnrollmentResponse enroll(Long courseId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new RuntimeException("Curso no encontrado")
                );

        enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .ifPresent(enrollment -> {
                    throw new RuntimeException("Ya estás inscrito en este curso");
                });

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        // Construir respuesta
        return buildResponse(enrollment);
    }

    // Obtener mis inscripciones
    public List<EnrollmentResponse> getMyEnrollments() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        return enrollmentRepository.findByUserId(user.getId())
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    // Cancelar inscripción
    public void deleteEnrollment(Long id) {

        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Inscripción no encontrada")
                );

        enrollmentRepository.delete(enrollment);
    }

    // Construir respuesta
    private EnrollmentResponse buildResponse(Enrollment enrollment) {

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .build();
    }

}