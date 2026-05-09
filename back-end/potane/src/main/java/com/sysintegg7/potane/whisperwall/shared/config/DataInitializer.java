package com.sysintegg7.potane.whisperwall.shared.config;

import com.sysintegg7.potane.whisperwall.shared.model.Role;
import com.sysintegg7.potane.whisperwall.shared.model.User;
import com.sysintegg7.potane.whisperwall.shared.repository.RoleRepository;
import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    private static final String DEFAULT_ADMIN_EMAIL = "admin2004@whisperwall.com";
    private static final String DEFAULT_ADMIN_USERNAME = "admin2004";
    private static final String DEFAULT_ADMIN_PASSWORD = "Kpota2004**";

    private static final String DEFAULT_MODERATOR_EMAIL = "moderator1004@whisperwall.com";
    private static final String DEFAULT_MODERATOR_USERNAME = "moderator1004";
    private static final String DEFAULT_MODERATOR_PASSWORD = "Kpota1004**";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminAccount();
        initializeModeratorAccount();
        enableRestrictionRequestsRls();
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

    private void initializeModeratorAccount() {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new RuntimeException("MODERATOR role not found"));

        User moderatorUser = userRepository.findByEmail(DEFAULT_MODERATOR_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setUsername(DEFAULT_MODERATOR_USERNAME);
            user.setEmail(DEFAULT_MODERATOR_EMAIL);
            user.setDisplayName("Moderator");
            user.setIsVerified(true);
            user.setIsActive(true);
            return user;
        });

        moderatorUser.setPassword(passwordEncoder.encode(DEFAULT_MODERATOR_PASSWORD));
        moderatorUser.setIsVerified(true);
        moderatorUser.setIsActive(true);

        Set<Role> roles = new HashSet<>(moderatorUser.getRoles());
        roles.add(userRole);
        roles.add(moderatorRole);
        moderatorUser.setRoles(roles);

        userRepository.save(moderatorUser);
        log.info("Moderator account is ready: {}", DEFAULT_MODERATOR_EMAIL);
    }

    private void initializeAdminAccount() {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        User adminUser = userRepository.findByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setUsername(DEFAULT_ADMIN_USERNAME);
            user.setEmail(DEFAULT_ADMIN_EMAIL);
            user.setDisplayName("System Admin");
            user.setIsVerified(true);
            user.setIsActive(true);
            return user;
        });

        adminUser.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        adminUser.setIsVerified(true);
        adminUser.setIsActive(true);

        Set<Role> roles = new HashSet<>(adminUser.getRoles());
        roles.add(userRole);
        roles.add(adminRole);
        adminUser.setRoles(roles);

        userRepository.save(adminUser);
        log.info("Admin account is ready: {}", DEFAULT_ADMIN_EMAIL);
    }

    private void enableRestrictionRequestsRls() {
        try {
            if (jdbcTemplate.getDataSource() == null) {
                log.warn("No DataSource available, skipping RLS enable");
                return;
            }
            try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
                String product = conn.getMetaData().getDatabaseProductName();
                if (product != null && product.toLowerCase().contains("postgres")) {
                    try {
                        jdbcTemplate.execute("ALTER TABLE IF EXISTS public.restriction_requests ENABLE ROW LEVEL SECURITY");
                        log.info("Enabled RLS for restriction_requests");
                    } catch (Exception e) {
                        log.warn("Failed to enable RLS for restriction_requests: {}", e.getMessage());
                    }
                } else {
                    log.info("Skipping RLS enable: database is not PostgreSQL (detected: {})", product);
                }
            }
        } catch (Exception e) {
            log.warn("Could not determine database product name, skipping RLS enable: {}", e.getMessage());
        }
    }
}
