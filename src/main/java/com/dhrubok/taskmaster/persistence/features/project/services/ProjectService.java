package com.dhrubok.taskmaster.persistence.features.project.services;

import com.dhrubok.taskmaster.common.exceptions.ApplicationException;
import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.project.enums.ProjectStatus;
import com.dhrubok.taskmaster.persistence.features.project.models.CreateProjectRequest;
import com.dhrubok.taskmaster.persistence.features.project.models.ProjectResponse;
import com.dhrubok.taskmaster.persistence.features.project.models.UpdateProjectRequest;
import com.dhrubok.taskmaster.persistence.features.project.repositories.ProjectRepository;
import com.dhrubok.taskmaster.persistence.features.projectmember.entities.ProjectMember;
import com.dhrubok.taskmaster.persistence.features.projectmember.enums.ProjectRole;
import com.dhrubok.taskmaster.persistence.features.projectmember.repositories.ProjectMemberRepository;
import com.dhrubok.taskmaster.persistence.features.task.repositories.TaskRepository;
import com.dhrubok.taskmaster.persistence.features.user.models.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(String managerEmail, CreateProjectRequest request) {
        log.info("Manager {} creating project: {}", managerEmail, request.getProjectName());

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can create projects");
        }

        Project project = Project.builder()
                .projectName(request.getProjectName())
                .description(request.getDescription())
                .managerUsername(managerEmail)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ProjectStatus.ACTIVE)
                .build();

        projectRepository.save(project);
        log.info("Project created with ID: {}", project.getId());

        // Add manager as project owner
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(manager);
        projectMember.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(projectMember);

        log.info("Manager {} added as OWNER of project {}", managerEmail, project.getId());

        return mapToProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjectsForUser(String email) {
        log.info("Fetching all projects for user: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == RoleType.MANAGER) {
            // Manager sees all projects
            return projectRepository.findAllByCreatedBy(email).stream()
                    .map(this::mapToProjectResponse)
                    .collect(Collectors.toList());
        } else {
            // Member sees only assigned projects
            List<ProjectMember> memberships = projectMemberRepository.findByUser(user);
            return memberships.stream()
                    .map(ProjectMember::getProject)
                    .map(this::mapToProjectResponse)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(String email, String projectId) {
        log.info("User {} fetching project: {}", email, projectId);

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

        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(String managerEmail, String projectId, UpdateProjectRequest request) {
        log.info("Manager {} updating project: {}", managerEmail, projectId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can update projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        projectRepository.save(project);
        log.info("Project {} updated successfully", projectId);

        return mapToProjectResponse(project);
    }

    @Transactional
    public void archiveProject(String managerEmail, String projectId) {
        log.info("Manager {} archiving project: {}", managerEmail, projectId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can archive projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);

        log.info("Project {} archived successfully", projectId);
    }

    @Transactional
    public void deleteProject(String managerEmail, String projectId) {
        log.info("Manager {} deleting project: {}", managerEmail, projectId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can delete projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Delete all project members
        projectMemberRepository.deleteByProject(project);

        // Delete project
        projectRepository.delete(project);

        log.info("Project {} deleted successfully", projectId);
    }

    @Transactional
    public void addMemberToProject(String managerEmail, String projectId, String memberId) {
        log.info("Manager {} adding member {} to project {}", managerEmail, memberId, projectId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can add members to projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (member.getRole() != RoleType.MEMBER) {
            throw new ApplicationException("Can only add MEMBER users to projects");
        }

        // Check if already a member
        if (projectMemberRepository.existsByProjectAndUser(project, member)) {
            throw new ApplicationException("User is already a member of this project");
        }

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(member);
        projectMember.setRole(ProjectRole.MEMBER);
        projectMemberRepository.save(projectMember);

        log.info("Member {} added to project {} successfully", memberId, projectId);
    }

    @Transactional
    public void removeMemberFromProject(String managerEmail, String projectId, String memberId) {
        log.info("Manager {} removing member {} from project {}", managerEmail, memberId, projectId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can remove members from projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        ProjectMember projectMember = projectMemberRepository.findByProjectAndUser(project, member)
                .orElseThrow(() -> new ResourceNotFoundException("Member is not part of this project"));

        // Cannot remove OWNER
        if (projectMember.getRole() == ProjectRole.OWNER) {
            throw new ApplicationException("Cannot remove project owner");
        }

        projectMemberRepository.delete(projectMember);
        log.info("Member {} removed from project {} successfully", memberId, projectId);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getProjectMembers(String email, String projectId) {
        log.info("User {} fetching members of project: {}", email, projectId);

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

        List<ProjectMember> members = projectMemberRepository.findByProject(project);
        return members.stream()
                .map(pm -> mapToUserResponse(pm.getUser()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectStats getProjectStats(String managerEmail, String projectId) {
        log.info("Manager {} fetching stats for project: {}", managerEmail, projectId);

        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != RoleType.MANAGER) {
            throw new ApplicationException("Only MANAGER can view project statistics");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        long totalTasks = taskRepository.countByProject(project);
        long completedTasks = taskRepository.countByProjectAndStatusCompleted(project);
        long inProgressTasks = taskRepository.countByProjectAndStatusInProgress(project);
        long todoTasks = taskRepository.countByProjectAndStatusTodo(project);
        long memberCount = projectMemberRepository.countByProject(project);

        return new ProjectStats(totalTasks, completedTasks, inProgressTasks, todoTasks, memberCount);
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        return ProjectResponse.builder()
                .projectId(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }

    public static class ProjectStats {
        public final Long totalTasks;
        public final Long completedTasks;
        public final Long inProgressTasks;
        public final Long todoTasks;
        public final Long memberCount;

        public ProjectStats(Long total, Long completed, Long inProgress, Long todo, Long members) {
            this.totalTasks = total;
            this.completedTasks = completed;
            this.inProgressTasks = inProgress;
            this.todoTasks = todo;
            this.memberCount = members;
        }
    }
}