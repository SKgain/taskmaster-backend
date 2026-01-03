package com.dhrubok.taskmaster.persistence.features.meeting.models;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeetingRequest {

    private String title;

    private String description;

    private LocalDateTime scheduledTime;

    @Positive(message = "Duration must be positive")
    private Integer duration;

    private String meetingLink;

    private String location;
}