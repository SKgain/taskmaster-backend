package com.dhrubok.taskmaster.persistence.features.admin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse {
    // User Statistics
    private Integer totalUsers;
    private Integer totalAdmins;
    private Integer totalManagers;
    private Integer totalMembers;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Integer pendingVerifications;

    // Project Statistics
    private Integer totalProjects;
    private Integer activeProjects;
    private Integer completedProjects;
    private Integer projectsAtRisk;
    private Double avgProjectCompletion;

    // Task Statistics
    private Integer totalTasks;
    private Integer todoTasks;
    private Integer inProgressTasks;
    private Integer completedTasks;
    private Integer cancelledTasks;
    private Integer overdueTasks;
    private Double avgTaskCompletionRate;

    // System Health
    private String systemStatus;
    private Long databaseSize;
    private Integer activeConnections;
    private Double cpuUsage;
    private Double memoryUsage;

    // Activity Metrics
    private Integer todayLogins;
    private Integer weeklyActiveUsers;
    private Integer monthlyActiveUsers;
    private Map<String, Integer> dailyTaskCreation; // Last 7 days
}
