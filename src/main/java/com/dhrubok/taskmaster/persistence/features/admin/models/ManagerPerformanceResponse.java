package com.dhrubok.taskmaster.persistence.features.admin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerPerformanceResponse {
    private String managerId;
    private String fullName;
    private String email;
    private Integer totalProjects;
    private Integer activeProjects;
    private Integer completedProjects;
    private Integer totalMembers;
    private Integer totalTasks;
    private Integer completedTasks;
    private Double projectCompletionRate;
    private Double taskCompletionRate;
    private Double teamProductivity;
    private Integer overdueTasks;
}