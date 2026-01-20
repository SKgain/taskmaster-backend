package com.dhrubok.taskmaster.persistence.features.admin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivitySummary {
    private String userId;
    private String fullName;
    private String email;
    private String role;
    private String lastLoginAt;
    private Integer taskCompletedThisMonth;
    private Integer projectsManaged;
    private Boolean isActive;
}