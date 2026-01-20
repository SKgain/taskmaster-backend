package com.dhrubok.taskmaster.persistence.auth.models;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profileImage;
    private Boolean isActive;
    private Boolean isEnabled;
    private Boolean isEmailVerified;
    private RoleType role;

    public AuthUserResponse(User user, UserDetails userDetails, Boolean isEmailVerified) {
        this.id = user.getId();
        this.username = userDetails.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phoneNumber = user.getPhoneNumber();
        this.profileImage = user.getProfileImage();
        this.isEmailVerified = isEmailVerified;
        this.role = user.getRole();
    }
}
