package com.elearning.platform.controller;

import com.elearning.platform.dto.request.CreateCourseRequest;
import com.elearning.platform.dto.request.UpdateCourseRequest;
import com.elearning.platform.dto.response.CourseCardResponse;
import com.elearning.platform.dto.response.CourseDetailResponse;
import com.elearning.platform.services.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Crear curso
    @PostMapping
    public ResponseEntity<CourseDetailResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request
    ) {

        CourseDetailResponse course = courseService.createCourse(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    // Obtener todos los cursos
    @GetMapping
    public ResponseEntity<List<CourseCardResponse>> getAllCourses() {

        List<CourseCardResponse> courses = courseService.getAllCourses();

        return ResponseEntity.ok(courses);
    }

    // Obtener curso por id
    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailResponse> getCourseById(
            @PathVariable Long id
    ) {

        CourseDetailResponse course = courseService.getCourseById(id);

        return ResponseEntity.ok(course);
    }

    // Actualizar curso
    @PutMapping("/{id}")
    public ResponseEntity<CourseDetailResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request
    ) {

        CourseDetailResponse course =
                courseService.updateCourse(id, request);

        return ResponseEntity.ok(course);
    }

    // Eliminar curso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id
    ) {

        courseService.deleteCourse(id);

        return ResponseEntity.noContent().build();
    }

}