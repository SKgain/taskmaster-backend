package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.common.constants.ErrorCode;
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

    @Transactional
    public UserSettingsResponse getUserSettings(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user));

        return UserSettingsResponse.fromEntity(settings);
    }

    @Transactional
    public UserSettingsResponse updateUserSettings(String email, UpdateSettingsRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultSettings(user));

        settings.setEmailNotifications(request.getEmailNotifications());
        settings.setTaskReminders(request.getTaskReminders());
        settings.setReminderHours(request.getReminderHours());

        UserSettings savedSettings = userSettingsRepository.save(settings);

        return UserSettingsResponse.fromEntity(savedSettings);
    }

    private UserSettings createDefaultSettings(User user) {

        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setEmailNotifications(true);
        settings.setTaskReminders(true);
        settings.setReminderHours(24);

        return userSettingsRepository.save(settings);
    }

    @Transactional
    public UserSettingsResponse resetToDefault(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(UserSettings::new);

        settings.setUser(user);
        settings.setEmailNotifications(true);
        settings.setTaskReminders(true);
        settings.setReminderHours(24);

        UserSettings savedSettings = userSettingsRepository.save(settings);

        return UserSettingsResponse.fromEntity(savedSettings);
    }
}