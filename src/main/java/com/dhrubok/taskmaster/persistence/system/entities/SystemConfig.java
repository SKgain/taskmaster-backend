package com.dhrubok.taskmaster.persistence.system.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @Column(name = "id")
    private String id;

    // Editable Settings - FIXED COLUMN NAMES TO MATCH DATABASE
    @Column(name = "allow_registration", nullable = false)
    private boolean allowRegistration = true;

    @Column(name = "require_verification", nullable = false)
    private boolean requireVerification = true;

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode = false;

    @Column(name = "session_timeout_minutes", nullable = false)  // ⚠️ FIXED: was "session_timeout"
    private int sessionTimeout = 30;

    @Column(name = "max_login_attempts", nullable = false)
    private int maxLoginAttempts = 5;

    @Column(name = "require_2fa", nullable = false)
    private boolean require2FA = false;

    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;

    @Column(name = "report_time")
    private String reportTime = "09:00";

    // Read-only System Information
    @Column(name = "system_version")
    private String systemVersion = "1.0.0";

    @Column(name = "environment")
    private String environment = "Production";

    @Column(name = "database_type")  // ⚠️ FIXED: was "database"
    private String database = "PostgreSQL";

    @Column(name = "last_backup")
    private String lastBackup = "Never";

    @Column(name = "storage_used")
    private String storageUsed = "0 GB";

    @Column(name = "active_sessions")
    private int activeSessions = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}