package com.dhrubok.taskmaster.persistence.features.task.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsResponse {
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int cancelledTasks;

    private int lowPriorityTasks;
    private int mediumPriorityTasks;
    private int highPriorityTasks;
    private int urgentTasks;

    private int overdueTasks;
    private int dueTodayTasks;
    private int dueThisWeekTasks;

    private Map<String, Integer> statusDistribution;
    private Map<String, Integer> priorityDistribution;
}