package com.dhrubok.taskmaster.persistence.features.task.repositories;

import com.dhrubok.taskmaster.persistence.features.task.entities.TaskActivity;
import com.dhrubok.taskmaster.persistence.features.task.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, String> {
    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(String taskId);
    List<TaskActivity> findByTaskIdAndActivityTypeOrderByCreatedAtDesc(String taskId, ActivityType type);
}
