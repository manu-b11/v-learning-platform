package com.elearning.platform.repository;

import com.elearning.platform.entity.Course;
import com.elearning.platform.enums.LearningStyle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
  List<Course> findByLearningStyleAndActiveTrue(
        LearningStyle learningStyle
);
}