package com.dhrubok.taskmaster.persistence.features.task.services;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.task.entities.Task;
import com.dhrubok.taskmaster.persistence.features.task.entities.TaskActivity;
import com.dhrubok.taskmaster.persistence.features.task.enums.ActivityType;
import com.dhrubok.taskmaster.persistence.features.task.models.TaskActivityResponse;
import com.dhrubok.taskmaster.persistence.features.task.repositories.TaskActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskActivityService {
    private final TaskActivityRepository taskActivityRepository;

    // Log activity
    public void logActivity(Task task, User user, ActivityType type,
                            String fieldChanged, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setPerformedBy(user);
        activity.setActivityType(type);
        activity.setFieldChanged(fieldChanged);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activity.setDescription(buildDescription(type, fieldChanged, oldValue, newValue));
        activity.setCreatedAt(LocalDateTime.now());

        taskActivityRepository.save(activity);
    }

    // Build human-readable description
    private String buildDescription(ActivityType type, String field,
                                    String oldValue, String newValue) {
        return switch (type) {
            case STATUS_CHANGED -> String.format("changed status from %s to %s", oldValue, newValue);
            case PRIORITY_CHANGED -> String.format("changed priority from %s to %s", oldValue, newValue);
            case ASSIGNED -> String.format("assigned task to %s", newValue);
            case REASSIGNED -> String.format("reassigned task from %s to %s", oldValue, newValue);
            case DUE_DATE_CHANGED -> String.format("changed due date from %s to %s", oldValue, newValue);
            case COMPLETED -> "marked task as completed";
            case CREATED -> "created task";
            default -> String.format("updated %s", field);
        };
    }

    // Get activities for a task
    public List<TaskActivity> getTaskActivities(String taskId) {
        return taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    public TaskActivityResponse totaskActivityResponse(TaskActivity activity) {

        return TaskActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getDescription())
                .performedByName(activity.getPerformedBy().getFullName())
                .performedByUsername(activity.getPerformedBy().getUsername())
                .description(activity.getDescription())
                .fieldChanged(activity.getFieldChanged())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
