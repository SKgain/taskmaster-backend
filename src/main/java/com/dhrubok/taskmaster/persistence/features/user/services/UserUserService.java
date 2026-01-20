package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.common.constants.ErrorCode;
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

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return Response.getResponseEntity(
                true,
                SuccessCode.SUCCESS,
                mapToUserResponse(user));
    }

    @Transactional
    public UserResponse updateUserProfile(String email, UpdateProfileRequest request) {

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            String phoneNumber = request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber();
            user.setPhoneNumber(phoneNumber);
        }

        if (request.getProfileImage() != null && !request.getProfileImage().isBlank()) {
            user.setProfileImage(request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse uploadProfilePhoto(String email, MultipartFile file) throws IOException {
        fileStorageService.validateImageFile(file);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        if (user.getProfileImage() != null && user.getProfileImage().startsWith("/uploads")) {
            try {
                fileStorageService.deleteFile(user.getProfileImage());
            } catch (Exception e) {
                log.warn("Failed to delete old profile photo: {}", e.getMessage());
            }
        }

        String photoUrl = fileStorageService.storeFile(file, user.getId());

        user.setProfileImage(photoUrl);

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApplicationException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApplicationException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApplicationException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));

        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<User> users = userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();

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
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .profileImage(buildFullImageUrl(user.getProfileImage()))
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .broadCastTitle(user.getBroadCastTitle())
                .broadCastMessage(user.getBroadCastMessage())
                .build();
    }

    private String buildFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return "http://localhost:8080/api/" + path;
    }

    public Response removeBroadCastMessage(String email) {

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.ERROR_USER_NOT_FOUND));
        user.setBroadCastTitle(null);
        user.setBroadCastMessage(null);

        userRepository.save(user);

        return Response.getResponseEntity(true, SuccessCode.SUCCESS, mapToUserResponse(user));
    }
}