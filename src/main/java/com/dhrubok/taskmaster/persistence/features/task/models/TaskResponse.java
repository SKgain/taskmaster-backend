package com.dhrubok.taskmaster.persistence.features.task.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String taskId;

    private String title;
    private String description;
    private String status;
    private String priority;

    private LocalDate dueDate;
    private Integer estimatedHours;
    private Instant completedAt;

    private String projectId;
    private String projectName;

    private String assignedToId;
    private String assignedToName;

    private Instant createdAt;
    private Instant updatedAt;
}
