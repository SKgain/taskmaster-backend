package com.dhrubok.taskmaster.persistence.features.admin.services;

import com.dhrubok.taskmaster.common.constants.ErrorCode;
import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.admin.models.*;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.project.enums.ProjectStatus;
import com.dhrubok.taskmaster.persistence.features.project.repositories.ProjectRepository;
import com.dhrubok.taskmaster.persistence.features.task.entities.Task;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import com.dhrubok.taskmaster.persistence.features.task.repositories.TaskRepository;
import com.dhrubok.taskmaster.persistence.features.user.models.UserResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public SystemStatsResponse getSystemStats() {

        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.countByRole(RoleType.ADMIN);
        long totalManagers = userRepository.countByRole(RoleType.MANAGER);
        long totalMembers = userRepository.countByRole(RoleType.MEMBER);
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;
        long pendingVerifications = userRepository.countByIsEmailVerifiedFalse();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        double memoryUsage = ((totalMemory - freeMemory) * 100.0) / totalMemory;

        return SystemStatsResponse.builder()
                .totalUsers((int) totalUsers)
                .totalAdmins((int) totalAdmins)
                .totalManagers((int) totalManagers)
                .totalMembers((int) totalMembers)
                .activeUsers((int) activeUsers)
                .inactiveUsers((int) inactiveUsers)
                .pendingVerifications((int) pendingVerifications)
                .systemStatus("HEALTHY")
                .memoryUsage(memoryUsage)
                .build();
    }

    @Override
    public SystemHealthResponse getSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        double memoryUsage = ((totalMemory - freeMemory) * 100.0) / totalMemory;

        String status = "HEALTHY";
        if (memoryUsage > 90) status = "CRITICAL";
        else if (memoryUsage > 75) status = "WARNING";

        Map<String, String> services = new HashMap<>();
        services.put("database", "ONLINE");
        services.put("email", "ONLINE");
        services.put("storage", "ONLINE");

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("memoryUsage", memoryUsage);
        metrics.put("totalMemory", totalMemory);
        metrics.put("freeMemory", freeMemory);

        return SystemHealthResponse.builder()
                .status(status)
                .uptime(System.currentTimeMillis())
                .version("1.0.0")
                .services(services)
                .metrics(metrics)
                .build();
    }

    @Override
    public List<Object> getAllUsers(String role, Boolean active) {
        List<User> users;

        if (role != null && active != null) {
            RoleType roleType = RoleType.valueOf(role.toUpperCase());
            users = userRepository.findAllByRoleAndIsActive(roleType, active);
        } else if (role != null) {
            RoleType roleType = RoleType.valueOf(role.toUpperCase());
            users = userRepository.findAllByRole(roleType);
        } else if (active != null) {
            users = active ? userRepository.findAllByIsActiveTrue() : userRepository.findAll();
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Object getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ERROR_USER_NOT_FOUND ));
        return convertToUserResponse(user);
    }

    @Override
    @Transactional
    public Object promoteToManager(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ERROR_USER_NOT_FOUND ));

        if (user.getRole() == RoleType.ADMIN) {
            throw new ApplicationException(ErrorCode.ERROR_CAN_NOT_CHANGE_ADMIN_ROLE);
        }

        if (user.getRole() == RoleType.MANAGER) {
            throw new ApplicationException(ErrorCode.ERROR_USER_IS_MANAGER);
        }

        user.setRole(RoleType.MANAGER);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        return convertToUserResponse(updated);
    }

    @Override
    @Transactional
    public Object demoteToMember(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ERROR_USER_NOT_FOUND ));

        if (user.getRole() == RoleType.ADMIN) {
            throw new ApplicationException(ErrorCode.ERROR_CAN_NOT_CHANGE_ADMIN_ROLE);
        }

        if (user.getRole() == RoleType.MEMBER) {
            throw new ApplicationException(ErrorCode.ERROR_USER_IS_MANAGER);
        }

        user.setRole(RoleType.MEMBER);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        return convertToUserResponse(updated);
    }

    @Override
    @Transactional
    public Object deactivateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ERROR_USER_NOT_FOUND ));

        if (user.getRole() == RoleType.ADMIN) {
            throw new ApplicationException(ErrorCode.ERROR_CAN_NOT_DEACTIVATE_MANAGER);
        }

        if (!user.getIsActive()) {
            throw new ApplicationException(ErrorCode.ERROR_USER_ALREADY_DEACTIVATE);
        }

        user.setIsActive(false);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        return convertToUserResponse(updated);
    }

    @Override
    @Transactional
    public Object activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ERROR_USER_NOT_FOUND ));

        if (user.getIsActive()) {
            throw new ApplicationException(ErrorCode.ERROR_USER_ALREADY_ACTIVATE);
        }

        user.setIsActive(true);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        return convertToUserResponse(updated);
    }

    @Override
    @Transactional
    public void broadcastNotification(String title, String message, String role) {
        int updatedCount = 0;

        if (role != null && !role.isEmpty()) {
            try {
                RoleType roleType = RoleType.valueOf(role.toUpperCase());
                updatedCount = userRepository.updateBroadcastForRole(roleType, title, message);
            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException("Invalid role: " + role);
            }
        } else {
            updatedCount = userRepository.updateBroadcastForAll(title, message);
        }
    }

    // ==================== HELPER METHODS ====================

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .department(user.getDepartment())
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .lastLoginAt(user.getLastLogin())
                .build();
    }
}