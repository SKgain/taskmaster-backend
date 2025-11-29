package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.common.constants.SuccessCode;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.user.models.UpdateProfileRequest;
import com.dhrubok.taskmaster.persistence.features.user.models.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUserService {

    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Transactional(readOnly = true)
    public Response getUserProfile(String email) {
        log.info("Fetching profile for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return Response.getResponseEntity(true, SuccessCode.SUCCESS,mapper.map(user, UserResponse.class));
    }

    @Transactional
    public UserResponse updateUserProfile(String email, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", email);

        return mapToUserResponse(updatedUser);
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
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }
}