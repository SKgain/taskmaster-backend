package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.user.entities.UserSettings;
import com.dhrubok.taskmaster.persistence.features.user.models.UpdateSettingsRequest;
import com.dhrubok.taskmaster.persistence.features.user.models.UserSettingsResponse;
import com.dhrubok.taskmaster.persistence.features.user.repositories.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    /**
     * Get user settings by email. Creates default settings if not exists.
     */
    @Transactional
    public UserSettingsResponse getUserSettings(String email) {
        log.info("Getting settings for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user));

        return UserSettingsResponse.fromEntity(settings);
    }

    /**
     * Update user settings
     */
    @Transactional
    public UserSettingsResponse updateUserSettings(String email, UpdateSettingsRequest request) {
        log.info("Updating settings for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user));

        // Update settings
        settings.setEmailNotifications(request.getEmailNotifications());
        settings.setTaskReminders(request.getTaskReminders());
        settings.setReminderHours(request.getReminderHours());

        UserSettings savedSettings = userSettingsRepository.save(settings);

        log.info("Settings updated successfully for user: {}", email);
        return UserSettingsResponse.fromEntity(savedSettings);
    }

    /**
     * Create default settings for a user
     */
    private UserSettings createDefaultSettings(User user) {
        log.info("Creating default settings for user: {}", user.getEmail());

        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setEmailNotifications(true);
        settings.setTaskReminders(true);
        settings.setReminderHours(24);

        return userSettingsRepository.save(settings);
    }

    /**
     * Reset settings to default
     */
    @Transactional
    public UserSettingsResponse resetToDefault(String email) {
        log.info("Resetting settings to default for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserSettings());

        settings.setUser(user);
        settings.setEmailNotifications(true);
        settings.setTaskReminders(true);
        settings.setReminderHours(24);

        UserSettings savedSettings = userSettingsRepository.save(settings);

        log.info("Settings reset successfully for user: {}", email);
        return UserSettingsResponse.fromEntity(savedSettings);
    }
}