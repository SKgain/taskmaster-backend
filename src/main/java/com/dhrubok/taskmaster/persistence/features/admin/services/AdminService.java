package com.dhrubok.taskmaster.persistence.features.admin.services;

import com.dhrubok.taskmaster.persistence.features.admin.models.*;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    SystemStatsResponse getSystemStats();

    SystemHealthResponse getSystemHealth();

    List<Object> getAllUsers(String role, Boolean active);

    Object getUserById(String userId);

    Object promoteToManager(String userId);

    Object demoteToMember(String userId);

    Object activateUser(String userId);

    Object deactivateUser(String userId);

    void broadcastNotification(String title, String message, String role) throws MessagingException, IOException;
}