package com.dhrubok.taskmaster.core.configs;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.system.entities.SystemConfig;
import com.dhrubok.taskmaster.persistence.system.repositories.SystemConfigRepository;
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
    private final SystemConfigRepository systemConfigRepository;

    private static final String CONFIG_ID = "SYSTEM_CONFIG";

    // Default Admin Credentials
    private static final String DEFAULT_ADMIN_EMAIL = "admin@taskmaster.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "taskmaster@123456";
    private static final String DEFAULT_ADMIN_USERNAME = "Admin";
    private static final String DEFAULT_ADMIN_FULLNAME = "System Administrator";

    // Default Manager Credentials
    private static final String DEFAULT_MANAGER_EMAIL = "manager@taskmaster.com";
    private static final String DEFAULT_MANAGER_PASSWORD = "taskmaster@123456";
    private static final String DEFAULT_MANAGER_USERNAME = "Manager";
    private static final String DEFAULT_MANAGER_FULLNAME = "System Manager";

    // Default Member Credentials
    private static final String DEFAULT_MEMBER_EMAIL = "member@taskmaster.com";
    private static final String DEFAULT_MEMBER_PASSWORD = "taskmaster@123456";
    private static final String DEFAULT_MEMBER_USERNAME = "Member";
    private static final String DEFAULT_MEMBER_FULLNAME = "System Member";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=====================================================");
        log.info("INITIALIZING DEFAULT USERS");
        log.info("=====================================================");

        createDefaultSystemConfig();
        createAdminIfNotExists();
        createMemberIfNotExists();
        createManagerIfNotExists();

        log.info("=====================================================");
        log.info("DEFAULT USER INITIALIZATION COMPLETED");
        log.info("=====================================================");
    }

    private void createDefaultSystemConfig() {
        if (systemConfigRepository.count() == 0) {
            SystemConfig config = SystemConfig.builder()
                    .id(CONFIG_ID)
                    .allowRegistration(true)
                    .requireVerification(true)
                    .maintenanceMode(false)
                    .sessionTimeout(30)
                    .maxLoginAttempts(5)
                    .require2FA(false)
                    .emailNotifications(true)
                    .reportTime("09:00")
                    .systemVersion("1.0.0")
                    .environment("Production")
                    .database("PostgreSQL")
                    .lastBackup("Never")
                    .storageUsed("0 GB")
                    .activeSessions(0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            systemConfigRepository.save(config);
            log.info("DEFAULT SYSTEM CONFIG CREATED!");
        }
    }

    private void createAdminIfNotExists() {
        if (userRepository.existsByRole(RoleType.ADMIN)) {
            log.info("ADMIN user already exists. Skipping creation.");
            return;
        }

        User admin = User.builder()
                .id(UUID.randomUUID().toString())
                .username(DEFAULT_ADMIN_USERNAME)
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .fullName(DEFAULT_ADMIN_FULLNAME)
                .role(RoleType.ADMIN)
                .isActive(true)
                .isEnabled(true)
                .isEmailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("SYSTEM")
                .build();

        userRepository.save(admin);
        log.info("DEFAULT ADMIN USER CREATED SUCCESSFULLY!");
        log.info("Email: {}", DEFAULT_ADMIN_EMAIL);
        log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
    }

    private void createMemberIfNotExists() {
        if (userRepository.existsByEmail(DEFAULT_MEMBER_EMAIL)) {
            log.info("MEMBER user already exists. Skipping creation.");
            return;
        }

        User member = User.builder()
                .id(UUID.randomUUID().toString())
                .username(DEFAULT_MEMBER_USERNAME)
                .email(DEFAULT_MEMBER_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_MEMBER_PASSWORD))
                .fullName(DEFAULT_MEMBER_FULLNAME)
                .role(RoleType.MEMBER)
                .isActive(true)
                .isEnabled(true)
                .isEmailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(DEFAULT_MANAGER_EMAIL)
                .build();

        userRepository.save(member);
        log.info("DEFAULT MEMBER USER CREATED SUCCESSFULLY!");
        log.info("Email: {}", DEFAULT_MEMBER_EMAIL);
        log.info("Password: {}", DEFAULT_MEMBER_PASSWORD);
    }

    private void createManagerIfNotExists() {
        if (userRepository.existsByEmail(DEFAULT_MANAGER_EMAIL)) {
            log.info("MANAGER user already exists. Skipping creation.");
            return;
        }

        User manager = User.builder()
                .id(UUID.randomUUID().toString())
                .username(DEFAULT_MANAGER_USERNAME)
                .email(DEFAULT_MANAGER_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_MANAGER_PASSWORD))
                .fullName(DEFAULT_MANAGER_FULLNAME)
                .role(RoleType.MANAGER)
                .isActive(true)
                .isEnabled(true)
                .isEmailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("SYSTEM")
                .build();

        userRepository.save(manager);
        log.info("DEFAULT MANAGER USER CREATED SUCCESSFULLY!");
        log.info("Email: {}", DEFAULT_MANAGER_EMAIL);
        log.info("Password: {}", DEFAULT_MANAGER_PASSWORD);
    }
}