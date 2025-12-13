package com.dhrubok.taskmaster.persistence.features.user.repositories;

import com.dhrubok.taskmaster.persistence.features.user.entities.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
    Optional<UserSettings> findByUserId(String userId);
    Optional<UserSettings> findByUserEmail(String email);
}