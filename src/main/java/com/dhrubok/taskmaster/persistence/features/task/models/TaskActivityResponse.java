package com.dhrubok.taskmaster.persistence.features.task.models;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
public class TaskActivityResponse {
    private String id;
    private String activityType;
    private String performedByName;
    private String performedByUsername;
    private String description;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}