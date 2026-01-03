package com.dhrubok.taskmaster.persistence.features.meeting.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingStatsResponse {
    private long totalMeetings;
    private long upcomingMeetings;
    private long completedMeetings;
    private long cancelledMeetings;
}