package com.elearning.platform.repository;

import com.elearning.platform.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseIdOrderByOrderNumberAsc(Long courseId);

}