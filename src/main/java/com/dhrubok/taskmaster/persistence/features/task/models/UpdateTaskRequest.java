package com.dhrubok.taskmaster.persistence.features.task.models;

import com.dhrubok.taskmaster.persistence.features.task.enums.Priority;
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
    private Priority priority;
    private LocalDate dueDate;
    private Integer estimatedHours;
    private TaskStatus status;
}
