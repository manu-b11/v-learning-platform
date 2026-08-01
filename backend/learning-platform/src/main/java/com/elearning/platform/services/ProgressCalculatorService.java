package com.elearning.platform.services;

import com.elearning.platform.entity.Content;
import com.elearning.platform.entity.Course;
import com.elearning.platform.entity.EvaluationResult;
import com.elearning.platform.entity.Module;
import com.elearning.platform.entity.Progress;
import com.elearning.platform.entity.User;
import com.elearning.platform.repository.EvaluationResultRepository;
import com.elearning.platform.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressCalculatorService {

    private final ProgressRepository progressRepository;
    private final EvaluationResultRepository evaluationResultRepository;

    // Progreso de un contenido
    public Double getContentCompletionPercentage(
            Content content,
            User user
    ) {

        return progressRepository
                .findByUserIdAndContentId(user.getId(), content.getId())
                .map(Progress::getCompletionPercentage)
                .orElse(0.0);
    }

    // Contenido completado
    public Boolean isContentCompleted(
            Content content,
            User user
    ) {

        return progressRepository
                .findByUserIdAndContentId(user.getId(), content.getId())
                .map(Progress::getCompleted)
                .orElse(false);
    }

    // Progreso de un módulo
    public Integer getModuleProgress(
            Module module,
            User user
    ) {

        if (module.getContents().isEmpty()) {
            return 0;
        }

        long completedContents = module.getContents()
                .stream()
                .filter(content -> isContentCompleted(content, user))
                .count();

        return (int) ((completedContents * 100.0)
                / module.getContents().size());
    }

    // Módulo completado
    public Boolean isModuleCompleted(
            Module module,
            User user
    ) {

        if (module.getContents().isEmpty()) {
            return false;
        }

        boolean allContentsCompleted = module.getContents()
                .stream()
                .allMatch(content -> isContentCompleted(content, user));

        if (module.getEvaluations().isEmpty()) {
            return allContentsCompleted;
        }

        boolean allEvaluationsApproved = module.getEvaluations()
                .stream()
                .allMatch(evaluation -> evaluationResultRepository
                        .findByUserIdAndEvaluationId(
                                user.getId(),
                                evaluation.getId()
                        )
                        .map(EvaluationResult::getApproved)
                        .orElse(false)
                );

        return allContentsCompleted && allEvaluationsApproved;
    }

    // Progreso de un curso
    public Integer getCourseProgress(
            Course course,
            User user
    ) {

        int totalContents = course.getModules()
                .stream()
                .mapToInt(module -> module.getContents().size())
                .sum();

        if (totalContents == 0) {
            return 0;
        }

        long completedContents = course.getModules()
                .stream()
                .flatMap(module -> module.getContents().stream())
                .filter(content -> isContentCompleted(content, user))
                .count();

        return (int) ((completedContents * 100.0)
                / totalContents);
    }

    // Curso completado
    public Boolean isCourseCompleted(
            Course course,
            User user
    ) {

        if (course.getModules().isEmpty()) {
            return false;
        }

        return course.getModules()
                .stream()
                .allMatch(module -> isModuleCompleted(module, user));
    }

    // Minutos restantes del curso
    public Integer getRemainingMinutes(
            Course course,
            User user
    ) {

        return course.getModules()
                .stream()
                .flatMap(module -> module.getContents().stream())
                .filter(content -> !isContentCompleted(content, user))
                .map(Content::getDurationMinutes)
                .filter(minutes -> minutes != null)
                .reduce(0, Integer::sum);
    }

}