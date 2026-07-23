package com.elearning.platform.repository;

import com.elearning.platform.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByModuleId(Long moduleId);

}