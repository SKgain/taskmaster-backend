package com.dhrubok.taskmaster.persistence.features.projectmember.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAvailabilityResponse {
    private String userId;
    private String fullName;
    private boolean isAvailable;
    private String availabilityStatus;
    private int currentTaskCount;
    private int maxCapacity;
    private double capacityUtilization;
    private LocalDateTime nextAvailableDate;
}