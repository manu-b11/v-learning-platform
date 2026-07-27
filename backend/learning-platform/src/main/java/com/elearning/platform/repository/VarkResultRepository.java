package com.elearning.platform.repository;

import com.elearning.platform.entity.VarkResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VarkResultRepository
        extends JpaRepository<VarkResult, Long> {

    Optional<VarkResult> findByUserId(Long userId);

}