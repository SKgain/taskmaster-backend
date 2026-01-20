package com.dhrubok.taskmaster.persistence.features.task.models;

import com.dhrubok.taskmaster.persistence.features.task.enums.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    private String title;
    private String description;
    private TaskPriority taskPriority;      // Optional, default MEDIUM in service
    private LocalDate dueDate;
    private Integer estimatedHours;

    private String projectId;       // Required
    private String assignedToId;
}
