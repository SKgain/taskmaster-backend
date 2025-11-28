package com.dhrubok.taskmaster.persistence.features.comment.entities;

import com.dhrubok.taskmaster.common.entities.AuditModel;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.task.entities.Task;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "comments")
@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends AuditModel {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}