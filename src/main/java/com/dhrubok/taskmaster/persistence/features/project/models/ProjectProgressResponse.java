package com.dhrubok.taskmaster.persistence.features.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProgressResponse {
    private String projectId;
    private String projectName;
    private String description;

    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int cancelledTasks;

    private double progressPercentage;
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isOverdue;
    private int daysRemaining;

    private int teamSize;
    private List<String> memberNames;

    private String healthStatus; // ON_TRACK, AT_RISK, DELAYED, COMPLETED
}