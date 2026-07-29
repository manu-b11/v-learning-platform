package com.elearning.platform.repository;

import com.elearning.platform.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByUserId(Long userId);

    Optional<Progress> findByUserIdAndContentId(
            Long userId,
            Long contentId
    );

}