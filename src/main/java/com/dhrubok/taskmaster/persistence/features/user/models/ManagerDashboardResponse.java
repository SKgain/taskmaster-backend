package com.dhrubok.taskmaster.persistence.features.user.models;

import com.dhrubok.taskmaster.persistence.features.project.models.ProjectProgressResponse;
import com.dhrubok.taskmaster.persistence.features.projectmember.models.MemberWorkloadResponse;
import com.dhrubok.taskmaster.persistence.features.task.models.TaskStatisticsResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {
    private int totalProjects;
    private int activeProjects;
    private int completedProjects;
    private int totalMembers;
    private int activeMembers;
    private int totalTasks;
    private int completedTasks;

    private double overallProjectProgress;
    private double overallTaskCompletionRate;

    private List<ProjectProgressResponse> recentProjects;
    private List<MemberWorkloadResponse> teamWorkload;
    private TaskStatisticsResponse taskStatistics;

    private int overloadedMembers;
    private int projectsAtRisk;
    private int overdueTasksCount;
}