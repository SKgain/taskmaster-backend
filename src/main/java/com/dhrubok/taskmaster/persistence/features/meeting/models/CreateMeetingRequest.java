package com.dhrubok.taskmaster.persistence.features.meeting.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeetingRequest {

    @NotBlank(message = "Meeting title is required")
    private String title;

    private String description;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledTime;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    private String meetingLink;

    private String location;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    private List<String> participantIds;
}