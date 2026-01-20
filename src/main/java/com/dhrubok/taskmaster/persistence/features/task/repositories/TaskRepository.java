package com.dhrubok.taskmaster.persistence.features.task.repositories;

import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.task.entities.Task;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskPriority;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    // Find by User
    List<Task> findByAssignedTo(User user);
    List<Task> findByAssignedToId(String userId);
    List<Task> findByAssignedToIdAndStatusIn(String userId, List<TaskStatus> statuses);

    // Find by Project
    List<Task> findByProject(Project project);
    List<Task> findByProjectId(String projectId);
    List<Task> findByProjectIdIn(List<String> projectIds);
    List<Task> findByProjectIdAndStatus(String projectId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds")
    List<Task> findAllByProjectIds(@Param("projectIds") List<String> projectIds);

    // Find by Status
    List<Task> findByStatus(TaskStatus status);

    // Find by Priority
    List<Task> findByTaskPriority(TaskPriority taskPriority);
    List<Task> findByStatusAndTaskPriority(TaskStatus status, TaskPriority taskPriority);

    // Overdue tasks
    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
    List<Task> findByAssignedToAndDueDateBeforeAndStatusNot(User user, LocalDate date, TaskStatus status);

    // Search
    List<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    List<Task> findByAssignedToAndTitleContainingIgnoreCaseOrAssignedToAndDescriptionContainingIgnoreCase(
            User user1, String title, User user2, String description
    );

    // Count queries
    Long countByProject(Project project);
    Long countByProjectId(String projectId);
    Long countByProjectIdAndStatus(String projectId, TaskStatus status);
    Long countByStatus(TaskStatus status);
    Long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.status = 'COMPLETED'")
    Long countByProjectAndStatusCompleted(@Param("project") Project project);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.status = 'IN_PROGRESS'")
    Long countByProjectAndStatusInProgress(@Param("project") Project project);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.status = 'TODO'")
    Long countByProjectAndStatusTodo(@Param("project") Project project);

    // Delete operations
    void deleteByProjectId(String projectId);
}