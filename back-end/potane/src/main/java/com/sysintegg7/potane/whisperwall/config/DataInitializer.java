package com.sysintegg7.potane.whisperwall.config;

import com.sysintegg7.potane.whisperwall.model.Role;
import com.sysintegg7.potane.whisperwall.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Standard user role");
            roleRepository.save(userRole);
            log.info("Created USER role");
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            roleRepository.save(adminRole);
            log.info("Created ADMIN role");
        }

        if (roleRepository.findByName("MODERATOR").isEmpty()) {
            Role moderatorRole = new Role();
            moderatorRole.setName("MODERATOR");
            moderatorRole.setDescription("Moderator role");
            roleRepository.save(moderatorRole);
            log.info("Created MODERATOR role");
        }
    }
}
