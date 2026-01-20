package com.dhrubok.taskmaster.persistence.system.services;

import com.dhrubok.taskmaster.persistence.system.entities.SystemConfig;
import com.dhrubok.taskmaster.persistence.system.models.SystemConfigDTO;
import com.dhrubok.taskmaster.persistence.system.repositories.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    private static final String CONFIG_ID = "SYSTEM_CONFIG";

    @Transactional(readOnly = true)
    public SystemConfigDTO getCurrentConfiguration() {

        SystemConfig config = configRepository.findById(CONFIG_ID)
                .orElseGet(this::createDefaultConfiguration);

        return mapToDTO(config);
    }

    @Transactional
    public SystemConfigDTO updateConfiguration(SystemConfigDTO configDTO) {

        SystemConfig config = configRepository.findById(CONFIG_ID)
                .orElseGet(this::createDefaultConfiguration);

        config.setAllowRegistration(configDTO.getAllowRegistration());
        config.setRequireVerification(configDTO.getRequireVerification());
        config.setMaintenanceMode(configDTO.getMaintenanceMode());
        config.setSessionTimeout(configDTO.getSessionTimeout());
        config.setMaxLoginAttempts(configDTO.getMaxLoginAttempts());
        config.setRequire2FA(configDTO.getRequire2FA());
        config.setEmailNotifications(configDTO.getEmailNotifications());
        config.setReportTime(configDTO.getReportTime());
        config.setUpdatedAt(Instant.now());

        SystemConfig saved = configRepository.save(config);

        return mapToDTO(saved);
    }

    @Transactional
    public SystemConfigDTO resetToDefaults() {

        SystemConfig config = configRepository.findById(CONFIG_ID)
                .orElseGet(this::createDefaultConfiguration);

        config.setAllowRegistration(true);
        config.setRequireVerification(true);
        config.setMaintenanceMode(false);
        config.setSessionTimeout(30);
        config.setMaxLoginAttempts(5);
        config.setRequire2FA(false);
        config.setEmailNotifications(true);
        config.setReportTime("09:00");
        config.setUpdatedAt(Instant.now());

        SystemConfig saved = configRepository.save(config);

        return mapToDTO(saved);
    }

    private SystemConfig createDefaultConfiguration() {

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

        return configRepository.save(config);
    }

    private SystemConfigDTO mapToDTO(SystemConfig config) {

        return SystemConfigDTO.builder()
                .allowRegistration(config.isAllowRegistration())
                .requireVerification(config.isRequireVerification())
                .maintenanceMode(config.isMaintenanceMode())
                .sessionTimeout(config.getSessionTimeout())
                .maxLoginAttempts(config.getMaxLoginAttempts())
                .require2FA(config.isRequire2FA())
                .emailNotifications(config.isEmailNotifications())
                .reportTime(config.getReportTime())
                .systemVersion(config.getSystemVersion())
                .environment(config.getEnvironment())
                .database(config.getDatabase())
                .lastBackup(config.getLastBackup())
                .storageUsed(config.getStorageUsed())
                .activeSessions(config.getActiveSessions())
                .build();
    }
}