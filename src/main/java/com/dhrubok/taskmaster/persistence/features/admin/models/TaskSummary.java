package com.dhrubok.taskmaster.persistence.features.admin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummary {
    private String taskId;
    private String title;
    private String projectName;
    private String assignedTo;
    private String status;
    private String priority;
    private String dueDate;
    private Boolean isOverdue;
}