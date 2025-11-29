package com.dhrubok.taskmaster.persistence.features.user.models;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 100)
    private String fullName;

    private String phoneNumber;

    private String profileImage;
}
