package com.dhrubok.taskmaster.persistence.features.task.entities;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.task.enums.ActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "task_activities")
public class TaskActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User performedBy;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
