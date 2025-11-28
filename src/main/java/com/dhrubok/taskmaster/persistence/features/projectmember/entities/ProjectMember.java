package com.dhrubok.taskmaster.persistence.features.projectmember.entities;

import com.dhrubok.taskmaster.common.entities.AuditModel;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.projectmember.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Table(name = "project_members")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectMember extends AuditModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ProjectRole role = ProjectRole.MEMBER;

    private Instant joinedAt = Instant.now();
}