package com.elearning.platform.repository;

import com.elearning.platform.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPointRepository
        extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUserId(Long userId);

}