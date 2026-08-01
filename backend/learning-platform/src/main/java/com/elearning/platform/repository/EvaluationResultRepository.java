package com.elearning.platform.repository;

import com.elearning.platform.entity.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationResultRepository
        extends JpaRepository<EvaluationResult, Long> {

    List<EvaluationResult> findByUserId(Long userId);

    Optional<EvaluationResult> findByUserIdAndEvaluationId(
            Long userId,
            Long evaluationId
    );

    List<EvaluationResult> findByEvaluationId(Long evaluationId);

}