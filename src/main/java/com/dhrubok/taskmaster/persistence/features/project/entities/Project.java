package com.dhrubok.taskmaster.persistence.features.project.entities;

import com.dhrubok.taskmaster.common.entities.AuditModel;
import com.dhrubok.taskmaster.persistence.features.project.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Project extends AuditModel {
    @Column(name = "manager_username")
    private String managerUsername;

    @Column(nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;
}