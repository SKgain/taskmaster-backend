package com.dhrubok.taskmaster.persistence.system.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {

    // Use Boolean instead of boolean to allow null
    @NotNull(message = "allowRegistration cannot be null")
    private Boolean allowRegistration;

    @NotNull(message = "requireVerification cannot be null")
    private Boolean requireVerification;

    @NotNull(message = "maintenanceMode cannot be null")
    private Boolean maintenanceMode;

    // Use Integer instead of int to allow null
    @NotNull(message = "sessionTimeout cannot be null")
    @Min(5)
    @Max(1440)
    private Integer sessionTimeout;

    @NotNull(message = "maxLoginAttempts cannot be null")
    @Min(3)
    @Max(10)
    private Integer maxLoginAttempts;

    @NotNull(message = "require2FA cannot be null")
    private Boolean require2FA;

    @NotNull(message = "emailNotifications cannot be null")
    private Boolean emailNotifications;

    private String reportTime;

    // Read-only fields (can be null when updating)
    private String systemVersion;
    private String environment;
    private String database;
    private String lastBackup;
    private String storageUsed;
    private Integer activeSessions;  // Use Integer instead of int
}