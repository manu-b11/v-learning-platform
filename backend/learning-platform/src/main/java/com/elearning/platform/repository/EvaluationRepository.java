package com.elearning.platform.repository;

import com.elearning.platform.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

  List<Evaluation> findByModuleIdOrderByOrderNumberAsc(Long moduleId);

}