package com.dhrubok.taskmaster.persistence.features.user.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String profileImage;
    private Boolean isActive;
    private Boolean isEmailVerified;
}