package com.dhrubok.taskmaster.persistence.features.admin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummary {
    private String projectId;
    private String projectName;
    private String managerName;
    private String status;
    private Double completionPercentage;
    private Integer totalTasks;
    private Integer completedTasks;
    private String deadline;
    private Boolean isAtRisk;
}