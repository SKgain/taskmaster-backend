package com.dhrubok.taskmaster.persistence.features.task.entities;

import com.dhrubok.taskmaster.common.entities.AuditModel;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskPriority;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends AuditModel {
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskPriority taskPriority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    private Integer estimatedHours;

    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    // FIX 1: Ignore Hibernate Proxy fields to prevent 500 Error
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false)
    // FIX 2: Ignore Proxy fields AND password/tokens to prevent Recursion & Security leaks
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "verificationToken", "refreshToken"})
    private User assignedTo;
}