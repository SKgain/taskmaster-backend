package com.dhrubok.taskmaster.persistence.features.project.entities;

import com.dhrubok.taskmaster.common.entities.AuditModel;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.project.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@EqualsAndHashCode(callSuper = true) // Important for entities extending a mapped superclass
public class Project extends AuditModel {

    // Note: ID, created_at, updated_at are inherited from AuditModel

    @Column(nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ProjectStatus status = ProjectStatus.ACTIVE;
}
