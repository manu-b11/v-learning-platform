package com.elearning.platform.repository;

import com.elearning.platform.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByName(String name);

    boolean existsByName(String name);

    List<Badge> findAllByOrderByRequiredPointsAsc();

}