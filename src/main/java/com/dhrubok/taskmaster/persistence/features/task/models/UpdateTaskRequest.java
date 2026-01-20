package com.dhrubok.taskmaster.persistence.features.task.models;

import com.dhrubok.taskmaster.persistence.features.task.enums.TaskPriority;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskPriority taskPriority;
    private LocalDate dueDate;
    private Integer estimatedHours;
    private TaskStatus status;
}
