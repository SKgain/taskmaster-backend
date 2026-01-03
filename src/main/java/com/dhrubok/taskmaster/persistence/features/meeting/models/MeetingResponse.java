package com.dhrubok.taskmaster.persistence.features.meeting.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {

    private String meetingId;

    private String title;

    private String description;

    private LocalDateTime scheduledTime;

    private Integer duration; // in minutes

    private String meetingLink;

    private String location;

    private String projectId;

    private String projectName;

    private String organizerId;

    private String organizerName;

    private List<String> participantIds;

    private List<String> participantNames;

    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    private Instant createdAt;

    // Computed fields for frontend convenience
    public boolean isUpcoming() {
        return scheduledTime.isAfter(LocalDateTime.now()) &&
                !"CANCELLED".equals(status) &&
                !"COMPLETED".equals(status);
    }

    public boolean isPast() {
        return scheduledTime.isBefore(LocalDateTime.now()) ||
                "COMPLETED".equals(status);
    }

    public String getFormattedDuration() {
        if (duration == null) return "N/A";

        int hours = duration / 60;
        int minutes = duration % 60;

        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }
}