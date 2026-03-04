package com.dhrubok.taskmaster.persistence.features.meeting.repositories;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.meeting.entities.nodes.Meeting;
import com.dhrubok.taskmaster.persistence.features.meeting.enums.MeetingStatus;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, String> {

    List<Meeting> findAllByOrganizer(User organizer);

    List<Meeting> findAllByParticipantsContaining(User participant);

    List<Meeting> findAllByProject(Project project);

    // Changed from Participant to Participants
    @Query("SELECT m FROM Meeting m WHERE m.organizer = :user AND m.scheduledTime > :now AND m.status = 'SCHEDULED' ORDER BY m.scheduledTime ASC")
    List<Meeting> findUpcomingMeetingsByOrganizer(@Param("user") User user, @Param("now") LocalDateTime now);

    // Changed from Participant to Participants
    @Query("SELECT m FROM Meeting m JOIN m.participants p WHERE p = :user AND m.scheduledTime > :now AND m.status = 'SCHEDULED' ORDER BY m.scheduledTime ASC")
    List<Meeting> findUpcomingMeetingsByParticipant(@Param("user") User user, @Param("now") LocalDateTime now);

    // Changed from Participant to Participants
    @Query("SELECT m FROM Meeting m WHERE m.organizer = :user AND m.scheduledTime < :now ORDER BY m.scheduledTime DESC")
    List<Meeting> findPastMeetingsByOrganizer(@Param("user") User user, @Param("now") LocalDateTime now);

    // Changed from Participant to Participants
    @Query("SELECT m FROM Meeting m JOIN m.participants p WHERE p = :user AND m.scheduledTime < :now ORDER BY m.scheduledTime DESC")
    List<Meeting> findPastMeetingsByParticipant(@Param("user") User user, @Param("now") LocalDateTime now);

    long countByOrganizer(User organizer);

    @Query("SELECT COUNT(m) FROM Meeting m WHERE m.organizer = :user AND m.scheduledTime > :now AND m.status = 'SCHEDULED'")
    long countUpcomingByOrganizer(@Param("user") User user, @Param("now") LocalDateTime now);

    long countByOrganizerAndStatus(User organizer, MeetingStatus status);

    // Changed from Participant to Participants
    @Query("SELECT m FROM Meeting m WHERE m.organizer = :user AND (LOWER(m.title) LIKE %:query% OR LOWER(m.description) LIKE %:query%) ORDER BY m.scheduledTime DESC")
    List<Meeting> searchByOrganizerAndQuery(@Param("user") User user, @Param("query") String query);

    // Changed from Participant to Participants
    @Query("SELECT m FROM Meeting m JOIN m.participants p WHERE p = :user AND (LOWER(m.title) LIKE %:query% OR LOWER(m.description) LIKE %:query%) ORDER BY m.scheduledTime DESC")
    List<Meeting> searchByParticipantAndQuery(@Param("user") User user, @Param("query") String query);
}