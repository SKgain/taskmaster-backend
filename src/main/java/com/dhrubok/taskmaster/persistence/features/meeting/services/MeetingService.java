package com.dhrubok.taskmaster.persistence.features.meeting.services;

import com.dhrubok.taskmaster.persistence.features.meeting.models.CreateMeetingRequest;
import com.dhrubok.taskmaster.persistence.features.meeting.models.MeetingResponse;
import com.dhrubok.taskmaster.persistence.features.meeting.models.MeetingStatsResponse;
import com.dhrubok.taskmaster.persistence.features.meeting.models.UpdateMeetingRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MeetingService {

    MeetingResponse createMeeting(String managerEmail, CreateMeetingRequest request);

    List<MeetingResponse> getAllMeetingsForUser(String userEmail);

    List<MeetingResponse> getMeetingsByProject(String userEmail, String projectId);

    MeetingResponse getMeetingById(String userEmail, String meetingId);

    MeetingResponse updateMeeting(String managerEmail, String meetingId, UpdateMeetingRequest request);

    void cancelMeeting(String managerEmail, String meetingId);

    void completeMeeting(String managerEmail, String meetingId);

    void addParticipant(String managerEmail, String meetingId, String userId);

    void removeParticipant(String managerEmail, String meetingId, String userId);

    void deleteMeeting(String managerEmail, String meetingId);

    List<MeetingResponse> getUpcomingMeetings(String userEmail);

    List<MeetingResponse> getPastMeetings(String userEmail);

    MeetingStatsResponse getMeetingStats(String managerEmail);

    List<MeetingResponse> searchMeetings(String userEmail, String query);
}