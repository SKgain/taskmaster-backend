package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.common.constants.SuccessCode;
import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.fileobject.services.FileStorageService;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.models.ChangePasswordRequest;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.user.models.UpdateProfileRequest;
import com.dhrubok.taskmaster.persistence.features.user.models.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUserService {

    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Response getUserProfile(String email) {
        log.info("Fetching profile for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return Response.getResponseEntity(true, SuccessCode.SUCCESS, mapToUserResponse(user));
    }

    @Transactional
    public UserResponse updateUserProfile(String email, UpdateProfileRequest request) {
        log.info("Updating profile for user: {} with data: {}", email, request);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Update full name
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            log.info("Updating fullName from '{}' to '{}'", user.getFullName(), request.getFullName());
            user.setFullName(request.getFullName());
        }

        // Update phone number - handle both null and empty string
        if (request.getPhoneNumber() != null) {
            String phoneNumber = request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber();
            log.info("Updating phoneNumber from '{}' to '{}'", user.getPhoneNumber(), phoneNumber);
            user.setPhoneNumber(phoneNumber);
        }

        // Update profile image URL (if provided directly)
        if (request.getProfileImage() != null && !request.getProfileImage().isBlank()) {
            log.info("Updating profileImage from '{}' to '{}'", user.getProfileImage(), request.getProfileImage());
            user.setProfileImage(request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}. New values - fullName: {}, phoneNumber: {}",
                email, updatedUser.getFullName(), updatedUser.getPhoneNumber());

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse uploadProfilePhoto(String email, MultipartFile file) throws IOException {
        log.info("Uploading profile photo for user: {}", email);

        // Validate file
        fileStorageService.validateImageFile(file);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Delete old photo if exists
        if (user.getProfileImage() != null && user.getProfileImage().startsWith("/uploads")) {
            try {
                fileStorageService.deleteFile(user.getProfileImage());
                log.info("Deleted old profile photo: {}", user.getProfileImage());
            } catch (Exception e) {
                log.warn("Failed to delete old profile photo: {}", e.getMessage());
            }
        }

        // Store new photo - pass the user ID as directory
        String photoUrl = fileStorageService.storeFile(file, user.getId());
        log.info("Stored new profile photo at: {}", photoUrl);

        user.setProfileImage(photoUrl);

        User updatedUser = userRepository.save(user);
        log.info("Profile photo uploaded successfully for user: {}", email);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApplicationException("Current password is incorrect");
        }

        // Verify new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApplicationException("New password and confirmation do not match");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApplicationException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", email);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        log.info("Searching users with query: {}", query);

        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<User> users = userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

        log.info("Found {} users matching query: {}", users.size(), query);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        log.info("Fetching all active users");

        List<User> users = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .collect(Collectors.toList());

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())  // ✅ Added this
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }
}