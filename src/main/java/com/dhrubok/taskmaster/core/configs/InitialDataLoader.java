package com.dhrubok.taskmaster.core.configs;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Initial Data Loader
 * Creates default ADMIN user on application startup if not exists
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Default Admin Credentials (Change these for production!)
    private static final String DEFAULT_ADMIN_EMAIL = "admin@taskmaster.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123456"; // Strong password
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_FULLNAME = "System Administrator";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=====================================================");
        log.info("Checking for ADMIN user in database...");
        log.info("=====================================================");

        // Check if any ADMIN user exists
        boolean adminExists = userRepository.existsByRole(RoleType.ADMIN);

        if (adminExists) {
            log.info("✓ ADMIN user already exists. Skipping creation.");
            return;
        }

        // Create default ADMIN user
        log.warn("⚠ No ADMIN user found. Creating default ADMIN account...");

        User admin = User.builder()
                .id(UUID.randomUUID().toString())
                .username(DEFAULT_ADMIN_USERNAME)
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .fullName(DEFAULT_ADMIN_FULLNAME)
                .role(RoleType.ADMIN)
                .isActive(true)
                .isEnabled(true) // Admin is enabled by default
                .isEmailVerified(true) // Admin email is pre-verified
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("SYSTEM")
                .build();

        userRepository.save(admin);

        log.info("=====================================================");
        log.info("✓ DEFAULT ADMIN USER CREATED SUCCESSFULLY!");
        log.info("=====================================================");
        log.info("Email: {}", DEFAULT_ADMIN_EMAIL);
        log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
        log.info("=====================================================");
        log.warn("⚠ IMPORTANT: Please change the admin password immediately after first login!");
        log.info("=====================================================");
    }
}