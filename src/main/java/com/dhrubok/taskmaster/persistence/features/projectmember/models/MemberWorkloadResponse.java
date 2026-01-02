package com.dhrubok.taskmaster.persistence.features.projectmember.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWorkloadResponse {
    private String userId;
    private String fullName;
    private String email;
    private String profileImage;

    // Task counts
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int overdueTasks;

    // Workload metrics
    private double workloadPercentage;
    private String workloadStatus; // LOW, MEDIUM, HIGH, OVERLOADED
    private int estimatedHoursTotal;
    private int estimatedHoursRemaining;

    // Performance
    private double completionRate;
    private double onTimeCompletionRate;
}