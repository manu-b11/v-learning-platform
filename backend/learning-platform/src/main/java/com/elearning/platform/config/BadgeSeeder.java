package com.elearning.platform.config;

import com.elearning.platform.entity.Badge;
import com.elearning.platform.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BadgeSeeder implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    public void run(String... args) {

        createBadge(
                "Primer Paso",
                "Has obtenido tus primeros 10 puntos y comenzado tu proceso de aprendizaje.",
                10
        );

        createBadge(
                "Aprendiz",
                "Has alcanzado 50 puntos demostrando constancia en tu progreso.",
                50
        );

        createBadge(
                "Experto",
                "Has alcanzado 100 puntos y consolidado tus conocimientos.",
                100
        );

        createBadge(
                "Maestro",
                "Has alcanzado 250 puntos y demostrado un dominio sobresaliente en la plataforma.",
                250
        );
    }

    private void createBadge(
            String name,
            String description,
            Integer requiredPoints
    ) {

        if (badgeRepository.existsByName(name)) {
            return;
        }

        Badge badge = Badge.builder()
                .name(name)
                .description(description)
                .requiredPoints(requiredPoints)
                .build();

        badgeRepository.save(badge);
    }

}