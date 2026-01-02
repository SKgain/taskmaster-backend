package com.dhrubok.taskmaster.persistence.features.task.services;

import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.common.services.EmailService;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.project.repositories.ProjectRepository;
import com.dhrubok.taskmaster.persistence.features.projectmember.repositories.ProjectMemberRepository;
import com.dhrubok.taskmaster.persistence.features.task.entities.Task;
import com.dhrubok.taskmaster.persistence.features.task.enums.ActivityType;
import com.dhrubok.taskmaster.persistence.features.task.enums.Priority;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import com.dhrubok.taskmaster.persistence.features.task.models.CreateTaskRequest;
import com.dhrubok.taskmaster.persistence.features.task.models.TaskResponse;
import com.dhrubok.taskmaster.persistence.features.task.models.UpdateTaskRequest;
import com.dhrubok.taskmaster.persistence.features.task.models.UpdateTaskStatusRequest;
import com.dhrubok.taskmaster.persistence.features.task.repositories.TaskRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TaskActivityService activityService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public TaskResponse createTask(String managerEmail, CreateTaskRequest request) {
        log.info("Manager {} creating task: {}", managerEmail, request.getTitle());

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can create tasks");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        User assignedMember = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + request.getAssignedToId()));

        // Verify member is part of the project
        if (!projectMemberRepository.existsByProjectAndUser(project, assignedMember)) {
            throw new ApplicationException("Member is not part of this project");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        task.setDueDate(request.getDueDate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setProject(project);
        task.setAssignedTo(assignedMember);

        taskRepository.save(task);
        log.info("Task created with ID: {}", task.getId());

        // Send email notification
        try {
            String taskUrl = frontendUrl + "/task-details.html?id=" + task.getId();
            emailService.sendTaskAssignedEmail(
                    assignedMember.getEmail(),
                    task.getTitle(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "No deadline",
                    manager.getFullName(),
                    taskUrl
            );
            log.info("Task assignment email sent to {}", assignedMember.getEmail());
        } catch (MessagingException | IOException e) {
            log.error("Failed to send task assignment email: {}", e.getMessage());
        }

        return mapToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasksForUser(String email) {
        log.info("Fetching all tasks for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == RoleType.MANAGER) {
            // Manager sees all tasks
            return taskRepository.findAll().stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        } else {
            // Member sees only assigned tasks
            return taskRepository.findByAssignedTo(user).stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(String email, String projectId) {
        log.info("User {} fetching tasks for project: {}", email, projectId);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Check access
        if (user.getRole() != RoleType.MANAGER) {
            boolean isMember = projectMemberRepository.existsByProjectAndUser(project, user);
            if (!isMember) {
                throw new ApplicationException("You don't have access to this project");
            }
        }

        return taskRepository.findByProject(project).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String email, String taskId) {
        log.info("User {} fetching task: {}", email, taskId);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // Check access
        if (user.getRole() != RoleType.MANAGER && !task.getAssignedTo().getId().equals(user.getId())) {
            throw new ApplicationException("You don't have access to this task");
        }

        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(String managerEmail, String taskId, UpdateTaskRequest request) {
        log.info("Manager {} updating task: {}", managerEmail, taskId);

        User user = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));


        if (user.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can update tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
            }
        }

        // Track changes and log activities
        if (!task.getStatus().equals(request.getStatus())) {
            activityService.logActivity(task, user, ActivityType.STATUS_CHANGED,
                    "status", task.getStatus().toString(), request.getStatus().toString());
        }

        if (!task.getPriority().equals(request.getPriority())) {
            activityService.logActivity(task, user, ActivityType.PRIORITY_CHANGED,
                    "priority", task.getPriority().toString(), request.getPriority().toString());
        }

        if (request.getDueDate() != null && !request.getDueDate().equals(task.getDueDate())) {
            activityService.logActivity(task, user, ActivityType.DUE_DATE_CHANGED,
                    "dueDate", task.getDueDate().toString(), request.getDueDate().toString());
        }

        taskRepository.save(task);
        log.info("Task {} updated successfully", taskId);

        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(String email, String taskId, UpdateTaskStatusRequest request) {
        log.info("User {} updating status of task: {}", email, taskId);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // Member can only update their own tasks, Manager can update any
        if (user.getRole() != RoleType.MANAGER && !task.getAssignedTo().getId().equals(user.getId())) {
            throw new ApplicationException("You can only update status of your own tasks");
        }

        task.setStatus(request.getStatus());
        if (request.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
        }

        taskRepository.save(task);
        log.info("Task {} status updated to {}", taskId, request.getStatus());

        return mapToTaskResponse(task);
    }

    @Transactional
    public void reassignTask(String managerEmail, String taskId, String newMemberId) {
        log.info("Manager {} reassigning task {} to member {}", managerEmail, taskId, newMemberId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can reassign tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        User newMember = userRepository.findById(newMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + newMemberId));

        // Verify member is part of the project
        if (!projectMemberRepository.existsByProjectAndUser(task.getProject(), newMember)) {
            throw new ApplicationException("Member is not part of this project");
        }

        task.setAssignedTo(newMember);
        taskRepository.save(task);

        // Send email notification
        try {
            String taskUrl = frontendUrl + "/task-details.html?id=" + task.getId();
            emailService.sendTaskAssignedEmail(
                    newMember.getEmail(),
                    task.getTitle(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "No deadline",
                    manager.getFullName(),
                    taskUrl
            );
            log.info("Task reassignment email sent to {}", newMember.getEmail());
        } catch (MessagingException | IOException e) {
            log.error("Failed to send task reassignment email: {}", e.getMessage());
        }

        log.info("Task {} reassigned to member {}", taskId, newMemberId);
    }

    @Transactional
    public void deleteTask(String managerEmail, String taskId) {
        log.info("Manager {} deleting task: {}", managerEmail, taskId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can delete tasks");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        taskRepository.delete(task);
        log.info("Task {} deleted successfully", taskId);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksAssignedToUser(String email) {
        log.info("Fetching tasks assigned to user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return taskRepository.findByAssignedTo(user).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(String email) {
        log.info("Fetching overdue tasks for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate today = LocalDate.now();

        if (user.getRole() == RoleType.MANAGER) {
            // Manager sees all overdue tasks
            return taskRepository.findByDueDateBeforeAndStatusNot(today, TaskStatus.COMPLETED).stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        } else {
            // Member sees only their overdue tasks
            return taskRepository.findByAssignedToAndDueDateBeforeAndStatusNot(user, today, TaskStatus.COMPLETED).stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public TaskStats getTaskStats(String managerEmail) {
        log.info("Manager {} fetching task statistics", managerEmail);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can view task statistics");
        }

        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long overdueTasks = taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.COMPLETED);

        return new TaskStats(totalTasks, completedTasks, inProgressTasks, todoTasks, overdueTasks);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(String email, String query) {
        log.info("User {} searching tasks with query: {}", email, query);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (query == null || query.isBlank()) {
            return List.of();
        }

        if (user.getRole() == RoleType.MANAGER) {
            // Manager searches all tasks
            return taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query).stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        } else {
            // Member searches only their tasks
            return taskRepository.findByAssignedToAndTitleContainingIgnoreCaseOrAssignedToAndDescriptionContainingIgnoreCase(
                            user, query, user, query
                    ).stream()
                    .map(this::mapToTaskResponse)
                    .collect(Collectors.toList());
        }
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .completedAt(task.getCompletedAt())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getProjectName())
                .assignedToId(task.getAssignedTo().getId())
                .assignedToName(task.getAssignedTo().getFullName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public static class TaskStats {
        public final Long totalTasks;
        public final Long completedTasks;
        public final Long inProgressTasks;
        public final Long todoTasks;
        public final Long overdueTasks;

        public TaskStats(Long total, Long completed, Long inProgress, Long todo, Long overdue) {
            this.totalTasks = total;
            this.completedTasks = completed;
            this.inProgressTasks = inProgress;
            this.todoTasks = todo;
            this.overdueTasks = overdue;
        }
    }
}