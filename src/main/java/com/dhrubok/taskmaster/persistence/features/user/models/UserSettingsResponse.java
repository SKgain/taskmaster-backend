package com.dhrubok.taskmaster.persistence.features.user.models;


import com.dhrubok.taskmaster.persistence.features.user.entities.UserSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {
    private String id;
    private Boolean emailNotifications;
    private Boolean taskReminders;
    private Integer reminderHours;
    private Instant creationDate;
    private Instant lastModifiedDate;

    public static UserSettingsResponse fromEntity(UserSettings settings) {
        return UserSettingsResponse.builder()
                .id(settings.getId())
                .emailNotifications(settings.getEmailNotifications())
                .taskReminders(settings.getTaskReminders())
                .reminderHours(settings.getReminderHours())
                .creationDate(settings.getCreatedAt())
                .lastModifiedDate(settings.getUpdatedAt())
                .build();
    }
}