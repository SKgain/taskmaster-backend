package com.dhrubok.taskmaster.persistence.features.meeting.services.impl;

import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.common.services.EmailService;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.meeting.MeetingRepository;
import com.dhrubok.taskmaster.persistence.features.meeting.entities.nodes.Meeting;
import com.dhrubok.taskmaster.persistence.features.meeting.enums.MeetingStatus;
import com.dhrubok.taskmaster.persistence.features.meeting.models.CreateMeetingRequest;
import com.dhrubok.taskmaster.persistence.features.meeting.models.MeetingResponse;
import com.dhrubok.taskmaster.persistence.features.meeting.models.MeetingStatsResponse;
import com.dhrubok.taskmaster.persistence.features.meeting.models.UpdateMeetingRequest;
import com.dhrubok.taskmaster.persistence.features.meeting.services.MeetingService;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.project.repositories.ProjectRepository;
import com.dhrubok.taskmaster.persistence.features.projectmember.repositories.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public MeetingResponse createMeeting(String managerEmail, CreateMeetingRequest request) {
        User manager = getUserByEmail(managerEmail);
        Project project = getProjectById(request.getProjectId());

        verifyProjectAccess(manager, project);

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .scheduledTime(request.getScheduledTime())
                .duration(request.getDuration())
                .meetingLink(request.getMeetingLink())
                .location(request.getLocation())
                .project(project)
                .organizer(manager)
                .participants(new ArrayList<>())
                .status(MeetingStatus.SCHEDULED)
                .build();

        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            List<User> participants = userRepository.findAllById(
                    new ArrayList<>(request.getParticipantIds())
            );
            meeting.setParticipants(participants);
        }

        Meeting savedMeeting = meetingRepository.save(meeting);

        if (!savedMeeting.getParticipants().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' h:mm a");
            String formattedTime = savedMeeting.getScheduledTime().format(formatter);

            String meetingLink = savedMeeting.getMeetingLink();
            String location = savedMeeting.getLocation();

            String displayLocation = (meetingLink != null && !meetingLink.isEmpty())
                    ? "Online Meeting"
                    : (location != null ? location : "See Details");

            String mainButtonUrl = (meetingLink != null && !meetingLink.isEmpty())
                    ? meetingLink
                    : frontendUrl + "/dashboard.html";

            for (User participant : savedMeeting.getParticipants()) {

                if(participant.getEmail().equals(manager.getEmail())) continue;

                try {
                    emailService.sendMeetingInvitationEmail(
                            participant.getEmail(),
                            savedMeeting.getTitle(),
                            formattedTime,
                            String.valueOf(savedMeeting.getDuration()),
                            manager.getFullName(),
                            displayLocation,
                            mainButtonUrl
                    );
                } catch (Exception e) {
                    log.error("Failed to send meeting invitation to {}: {}", participant.getEmail(), e.getMessage());
                }
            }
        }
        return toMeetingResponse(savedMeeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getAllMeetingsForUser(String userEmail) {
        User user = getUserByEmail(userEmail);

        List<Meeting> meetings;
        if (user.getRole().name().equals("MANAGER")) {
            meetings = meetingRepository.findAllByOrganizer(user);
        } else {
            meetings = meetingRepository.findAllByParticipantsContaining(user);
        }

        return meetings.stream()
                .map(this::toMeetingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetingsByProject(String userEmail, String projectId) {
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);

        verifyProjectAccess(user, project);

        List<Meeting> meetings = meetingRepository.findAllByProject(project);

        return meetings.stream()
                .filter(meeting -> canAccessMeeting(user, meeting))
                .map(this::toMeetingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponse getMeetingById(String userEmail, String meetingId) {
        User user = getUserByEmail(userEmail);
        Meeting meeting = getMeetingById(meetingId);

        if (!canAccessMeeting(user, meeting)) {
            throw new ApplicationException("You don't have access to this meeting");
        }

        return toMeetingResponse(meeting);
    }

    @Override
    public MeetingResponse updateMeeting(String managerEmail, String meetingId, UpdateMeetingRequest request) {
        User manager = getUserByEmail(managerEmail);
        Meeting meeting = getMeetingById(meetingId);

        verifyMeetingOwnership(manager, meeting);

        if (request.getTitle() != null) meeting.setTitle(request.getTitle());
        if (request.getDescription() != null) meeting.setDescription(request.getDescription());
        if (request.getScheduledTime() != null) meeting.setScheduledTime(request.getScheduledTime());
        if (request.getDuration() != null) meeting.setDuration(request.getDuration());
        if (request.getMeetingLink() != null) meeting.setMeetingLink(request.getMeetingLink());
        if (request.getLocation() != null) meeting.setLocation(request.getLocation());

        Meeting updatedMeeting = meetingRepository.save(meeting);

        return toMeetingResponse(updatedMeeting);
    }

    @Override
    public void cancelMeeting(String managerEmail, String meetingId) {
        User manager = getUserByEmail(managerEmail);
        Meeting meeting = getMeetingById(meetingId);

        verifyMeetingOwnership(manager, meeting);

        meeting.setStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);
    }

    @Override
    public void completeMeeting(String managerEmail, String meetingId) {
        User manager = getUserByEmail(managerEmail);
        Meeting meeting = getMeetingById(meetingId);

        verifyMeetingOwnership(manager, meeting);

        meeting.setStatus(MeetingStatus.COMPLETED);
        meetingRepository.save(meeting);
    }

    @Override
    public void addParticipant(String managerEmail, String meetingId, String userId) {
        User manager = getUserByEmail(managerEmail);
        Meeting meeting = getMeetingById(meetingId);
        User participant = getUserById(userId);

        verifyMeetingOwnership(manager, meeting);

        if (meeting.getParticipants().contains(participant)) {
            throw new ApplicationException("User is already a participant");
        }

        meeting.getParticipants().add(participant);
        meetingRepository.save(meeting);
    }

    @Override
    public void removeParticipant(String managerEmail, String meetingId, String userId) {
        User manager = getUserByEmail(managerEmail);
        Meeting meeting = getMeetingById(meetingId);
        User participant = getUserById(userId);

        verifyMeetingOwnership(manager, meeting);

        meeting.getParticipants().remove(participant);
        meetingRepository.save(meeting);
    }

    @Override
    public void deleteMeeting(String managerEmail, String meetingId) {
        User manager = getUserByEmail(managerEmail);
        Meeting meeting = getMeetingById(meetingId);

        verifyMeetingOwnership(manager, meeting);

        meetingRepository.delete(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getUpcomingMeetings(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        List<Meeting> meetings;
        if (user.getRole().name().equals("MANAGER")) {
            meetings = meetingRepository.findUpcomingMeetingsByOrganizer(user, now);
        } else {
            meetings = meetingRepository.findUpcomingMeetingsByParticipant(user, now);
        }

        return meetings.stream()
                .map(this::toMeetingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getPastMeetings(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        List<Meeting> meetings;
        if (user.getRole().name().equals("MANAGER")) {
            meetings = meetingRepository.findPastMeetingsByOrganizer(user, now);
        } else {
            meetings = meetingRepository.findPastMeetingsByParticipant(user, now);
        }

        return meetings.stream()
                .map(this::toMeetingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingStatsResponse getMeetingStats(String managerEmail) {
        User manager = getUserByEmail(managerEmail);

        long totalMeetings = meetingRepository.countByOrganizer(manager);
        long upcomingMeetings = meetingRepository.countUpcomingByOrganizer(manager, LocalDateTime.now());
        long completedMeetings = meetingRepository.countByOrganizerAndStatus(manager, MeetingStatus.COMPLETED);
        long cancelledMeetings = meetingRepository.countByOrganizerAndStatus(manager, MeetingStatus.CANCELLED);

        return MeetingStatsResponse.builder()
                .totalMeetings(totalMeetings)
                .upcomingMeetings(upcomingMeetings)
                .completedMeetings(completedMeetings)
                .cancelledMeetings(cancelledMeetings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> searchMeetings(String userEmail, String query) {
        User user = getUserByEmail(userEmail);

        List<Meeting> meetings;
        if (user.getRole().name().equals("MANAGER")) {
            meetings = meetingRepository.searchByOrganizerAndQuery(user, query.toLowerCase());
        } else {
            meetings = meetingRepository.searchByParticipantAndQuery(user, query.toLowerCase());
        }

        return meetings.stream()
                .map(this::toMeetingResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException("User not found: " + email));
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("User not found with ID: " + userId));
    }

    private Project getProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApplicationException("Project not found with ID: " + projectId));
    }

    private Meeting getMeetingById(String meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApplicationException("Meeting not found with ID: " + meetingId));
    }

    private void verifyProjectAccess(User user, Project project) {

        if (project.getManagerUsername().equals(user.getEmail())) {
            return;
        }

        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, user);

        if (!isMember) {
            throw new ApplicationException("You don't have access to this project");
        }
    }

    private void verifyMeetingOwnership(User manager, Meeting meeting) {
        if (!meeting.getOrganizer().equals(manager)) {
            throw new ApplicationException("Only the meeting organizer can perform this action");
        }
    }

    private boolean canAccessMeeting(User user, Meeting meeting) {
        return meeting.getOrganizer().equals(user) ||
                meeting.getParticipants().contains(user);
    }

    private MeetingResponse toMeetingResponse(Meeting meeting) {
        return MeetingResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .scheduledTime(meeting.getScheduledTime())
                .duration(meeting.getDuration())
                .meetingLink(meeting.getMeetingLink())
                .location(meeting.getLocation())
                .projectId(meeting.getProject().getId())
                .projectName(meeting.getProject().getProjectName())
                .organizerId(meeting.getOrganizer().getId())
                .organizerName(meeting.getOrganizer().getFullName())
                .participantIds(meeting.getParticipants().stream()
                        .map(User::getId)
                        .collect(Collectors.toList()))
                .participantNames(meeting.getParticipants().stream()
                        .map(User::getFullName)
                        .collect(Collectors.toList()))
                .status(meeting.getStatus().name())
                .createdAt(meeting.getCreatedAt())
                .build();
    }
}