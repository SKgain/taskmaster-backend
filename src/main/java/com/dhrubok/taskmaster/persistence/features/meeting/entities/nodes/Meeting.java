package com.dhrubok.taskmaster.persistence.features.meeting.entities.nodes;

import com.dhrubok.taskmaster.common.entities.AuditModel;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.meeting.enums.MeetingStatus;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meetings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Meeting extends AuditModel {
    private String title;
    private String description;
    private LocalDateTime scheduledTime;
    private Integer duration;
    private String meetingLink;
    private String location;

    @ManyToOne
    private Project project;

    @ManyToOne
    private User organizer;

    @ManyToMany
    private List<User> participants;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status;
}
