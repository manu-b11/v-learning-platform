package com.elearning.platform.controller;

import com.elearning.platform.dto.response.EnrollmentResponse;
import com.elearning.platform.services.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Inscribir usuario en un curso
    @PostMapping("/api/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable Long courseId
    ) {

        EnrollmentResponse enrollment =
                enrollmentService.enroll(courseId);

        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    // Obtener mis inscripciones
    @GetMapping("/api/enrollments/me")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {

        List<EnrollmentResponse> enrollments =
                enrollmentService.getMyEnrollments();

        return ResponseEntity.ok(enrollments);
    }

    // Cancelar inscripción
    @DeleteMapping("/api/enrollments/{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Long id
    ) {

        enrollmentService.deleteEnrollment(id);

        return ResponseEntity.noContent().build();
    }

}