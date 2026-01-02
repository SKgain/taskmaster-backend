package com.dhrubok.taskmaster.persistence.features.projectmember.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPerformanceResponse {
    private String userId;
    private String fullName;
    private String email;
    private String profileImage;

    private int totalTasksCompleted;
    private int totalTasksAssigned;
    private double completionRate;

    private int tasksCompletedOnTime;
    private int tasksCompletedLate;
    private double onTimeCompletionRate;
    private double averageCompletionDays;

    private int highPriorityTasksCompleted;
    private int urgentTasksCompleted;

    private LocalDateTime lastTaskCompletedAt;
    private int tasksCompletedThisWeek;
    private int tasksCompletedThisMonth;

    private String performanceTrend; // IMPROVING, STABLE, DECLINING
}