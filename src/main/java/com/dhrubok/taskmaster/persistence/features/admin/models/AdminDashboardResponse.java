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
public class AdminDashboardResponse {
    private SystemStatsResponse systemStats;
    private java.util.List<UserActivitySummary> recentActivity;
    private java.util.List<ProjectSummary> criticalProjects;
    private java.util.List<TaskSummary> urgentTasks;
    private Map<String, Object> performanceMetrics;
}