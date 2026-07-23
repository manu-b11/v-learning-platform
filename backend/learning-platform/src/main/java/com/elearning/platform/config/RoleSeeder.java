package com.elearning.platform.config;

import com.elearning.platform.entity.Role;
import com.elearning.platform.enums.RoleName;
import com.elearning.platform.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRole(RoleName.ADMIN);
        createRole(RoleName.INSTRUCTOR);
        createRole(RoleName.STUDENT);

    }

    private void createRole(RoleName roleName) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(
                    Role.builder()
                            .name(roleName)
                            .build()
            );
        }
    }
}