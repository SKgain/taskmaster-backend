package com.dhrubok.taskmaster.persistence.features.user.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsRequest {
    @NotNull(message = "Email notifications preference is required")
    private Boolean emailNotifications;

    @NotNull(message = "Task reminders preference is required")
    private Boolean taskReminders;

    @NotNull(message = "Reminder hours is required")
    @Min(value = 1, message = "Reminder hours must be at least 1")
    private Integer reminderHours;
}