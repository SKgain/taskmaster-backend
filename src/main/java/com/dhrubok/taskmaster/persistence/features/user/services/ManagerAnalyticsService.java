package com.dhrubok.taskmaster.persistence.features.user.services;

import com.dhrubok.taskmaster.common.exceptions.ResourceNotFoundException;
import com.dhrubok.taskmaster.persistence.auth.entities.User;
import com.dhrubok.taskmaster.persistence.auth.enums.RoleType;
import com.dhrubok.taskmaster.persistence.auth.repositories.UserRepository;
import com.dhrubok.taskmaster.persistence.features.project.entities.Project;
import com.dhrubok.taskmaster.persistence.features.project.enums.ProjectStatus;
import com.dhrubok.taskmaster.persistence.features.project.models.ProjectProgressResponse;
import com.dhrubok.taskmaster.persistence.features.project.repositories.ProjectRepository;
import com.dhrubok.taskmaster.persistence.features.projectmember.entities.ProjectMember;
import com.dhrubok.taskmaster.persistence.features.projectmember.models.MemberAvailabilityResponse;
import com.dhrubok.taskmaster.persistence.features.projectmember.models.MemberPerformanceResponse;
import com.dhrubok.taskmaster.persistence.features.projectmember.models.MemberWorkloadResponse;
import com.dhrubok.taskmaster.persistence.features.projectmember.repositories.ProjectMemberRepository;
import com.dhrubok.taskmaster.persistence.features.task.entities.Task;
import com.dhrubok.taskmaster.persistence.features.task.enums.Priority;
import com.dhrubok.taskmaster.persistence.features.task.enums.TaskStatus;
import com.dhrubok.taskmaster.persistence.features.task.models.TaskStatisticsResponse;
import com.dhrubok.taskmaster.persistence.features.task.repositories.TaskRepository;
import com.dhrubok.taskmaster.persistence.features.user.models.ManagerDashboardResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManagerAnalyticsService {
    private static final int DEFAULT_CAPACITY = 10;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // ==================== DASHBOARD OVERVIEW ====================

    public ManagerDashboardResponse getDashboardOverview(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // FIX: Use findByManagerUsername (which stores email)
        List<Project> allProjects = projectRepository.findByManagerUsername(manager.getEmail());

        int totalProjects = allProjects.size();
        int activeProjects = (int) allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.ACTIVE).count();
        int completedProjects = (int) allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();

        List<User> allMembers = userRepository.findByRole(RoleType.MEMBER);
        int totalMembers = allMembers.size();
        int activeMembers = (int) allMembers.stream().filter(user -> user.getIsActive().equals(true)).count();

        List<String> projectIds = allProjects.stream().map(Project::getId).collect(Collectors.toList());
        List<Task> allTasks = projectIds.isEmpty() ? Collections.emptyList() : taskRepository.findAllByProjectIds(projectIds);

        int totalTasks = allTasks.size();
        int completedTasks = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

        double projectProgress = totalProjects > 0 ? (completedProjects * 100.0 / totalProjects) : 0;
        double taskCompletion = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

        List<MemberWorkloadResponse> workloads = getMemberWorkloads(managerEmail);
        int overloaded = (int) workloads.stream().filter(w -> w.getWorkloadStatus().equals("OVERLOADED")).count();

        List<ProjectProgressResponse> progress = getProjectsProgress(managerEmail);
        int atRisk = (int) progress.stream().filter(p -> p.getHealthStatus().equals("AT_RISK") || p.getHealthStatus().equals("DELAYED")).count();

        LocalDate today = LocalDate.now();
        int overdueTasks = (int) allTasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != TaskStatus.COMPLETED).count();

        List<ProjectProgressResponse> recentProjects = progress.stream().limit(5).collect(Collectors.toList());
        TaskStatisticsResponse taskStats = getTaskStatistics(allTasks);

        return ManagerDashboardResponse.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .overallProjectProgress(Math.round(projectProgress * 10) / 10.0)
                .overallTaskCompletionRate(Math.round(taskCompletion * 10) / 10.0)
                .recentProjects(recentProjects)
                .teamWorkload(workloads)
                .taskStatistics(taskStats)
                .overloadedMembers(overloaded)
                .projectsAtRisk(atRisk)
                .overdueTasksCount(overdueTasks)
                .build();
    }

    // ==================== PROJECT PROGRESS ====================

    public List<ProjectProgressResponse> getProjectsProgress(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // FIX: Use findByManagerUsername (which stores email)
        List<Project> projects = projectRepository.findByManagerUsername(manager.getEmail());

        return projects.stream().map(this::calculateProjectProgress).collect(Collectors.toList());
    }

    private ProjectProgressResponse calculateProjectProgress(Project project) {
        List<Task> tasks = taskRepository.findByProjectId(project.getId());
        int total = tasks.size();
        int todo = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        int inProgress = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int completed = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        int cancelled = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.CANCELLED).count();

        double progress = total > 0 ? (completed * 100.0 / total) : 0;
        String status = project.getStatus().toString();
        boolean isOverdue = project.getEndDate() != null && project.getEndDate().isBefore(LocalDate.now()) && project.getStatus() != ProjectStatus.COMPLETED;
        int daysRemaining = project.getEndDate() != null ? (int) ChronoUnit.DAYS.between(LocalDate.now(), project.getEndDate()) : 0;
        String health = calculateProjectHealth(progress, daysRemaining, isOverdue, project.getStatus());

        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        List<String> memberNames = members.stream().map(pm -> pm.getUser().getFullName()).collect(Collectors.toList());

        return ProjectProgressResponse.builder()
                .projectId(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .totalTasks(total)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .completedTasks(completed)
                .cancelledTasks(cancelled)
                .progressPercentage(Math.round(progress * 10) / 10.0)
                .status(status)
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .isOverdue(isOverdue)
                .daysRemaining(daysRemaining)
                .teamSize(members.size())
                .memberNames(memberNames)
                .healthStatus(health)
                .build();
    }

    private String calculateProjectHealth(double progress, int daysRemaining, boolean isOverdue, ProjectStatus status) {
        if (status == ProjectStatus.COMPLETED) return "COMPLETED";
        if (isOverdue) return "DELAYED";
        if (daysRemaining < 7 && progress < 80) return "AT_RISK";
        if (daysRemaining < 14 && progress < 60) return "AT_RISK";
        return "ON_TRACK";
    }

    // ==================== TASK DISTRIBUTION ====================

    public Map<String, Integer> getTaskStatusDistribution(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // FIX: Use findByManagerUsername (which stores email)
        List<Project> projects = projectRepository.findByManagerUsername(manager.getEmail());

        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<Task> tasks = projectIds.isEmpty() ? Collections.emptyList() : taskRepository.findAllByProjectIds(projectIds);

        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("TODO", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count());
        distribution.put("IN_PROGRESS", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count());
        distribution.put("COMPLETED", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
        distribution.put("CANCELLED", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.CANCELLED).count());
        return distribution;
    }

    // ==================== MEMBER WORKLOAD ====================

    public List<MemberWorkloadResponse> getMemberWorkloads(String managerEmail) {
        List<User> members = userRepository.findByRoleAndIsActiveTrue(RoleType.MEMBER);
        return members.stream()
                .map(this::calculateMemberWorkload)
                .sorted((a, b) -> Double.compare(b.getWorkloadPercentage(), a.getWorkloadPercentage()))
                .collect(Collectors.toList());
    }

    private MemberWorkloadResponse calculateMemberWorkload(User member) {
        List<Task> allTasks = taskRepository.findByAssignedToId(member.getId());
        int total = allTasks.size();
        int todo = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        int inProgress = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int completed = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        int overdue = (int) allTasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.COMPLETED).count();

        int activeTasks = todo + inProgress;
        double workloadPercentage = (activeTasks * 100.0) / DEFAULT_CAPACITY;

        String workloadStatus;
        if (workloadPercentage >= 100) workloadStatus = "OVERLOADED";
        else if (workloadPercentage >= 75) workloadStatus = "HIGH";
        else if (workloadPercentage >= 40) workloadStatus = "MEDIUM";
        else workloadStatus = "LOW";

        int totalHours = allTasks.stream().filter(t -> t.getEstimatedHours() != null).mapToInt(Task::getEstimatedHours).sum();
        int remainingHours = allTasks.stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getEstimatedHours() != null).mapToInt(Task::getEstimatedHours).sum();
        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;

        return MemberWorkloadResponse.builder()
                .userId(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .profileImage(member.getProfileImage())
                .totalTasks(total).todoTasks(todo).inProgressTasks(inProgress).completedTasks(completed).overdueTasks(overdue)
                .workloadPercentage(Math.round(workloadPercentage * 10) / 10.0)
                .workloadStatus(workloadStatus)
                .estimatedHoursTotal(totalHours).estimatedHoursRemaining(remainingHours)
                .completionRate(Math.round(completionRate * 10) / 10.0)
                .build();
    }

    // ==================== MEMBER PERFORMANCE ====================

    public List<MemberPerformanceResponse> getMemberPerformance(String managerEmail) {
        List<User> members = userRepository.findByRoleAndIsActiveTrue(RoleType.MEMBER);
        return members.stream()
                .map(this::calculateMemberPerformance)
                .sorted((a, b) -> Double.compare(b.getCompletionRate(), a.getCompletionRate()))
                .collect(Collectors.toList());
    }

    private MemberPerformanceResponse calculateMemberPerformance(User member) {
        List<Task> allTasks = taskRepository.findByAssignedToId(member.getId());
        List<Task> completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).toList();

        int total = allTasks.size();
        int completed = completedTasks.size();
        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;

        int onTime = (int) completedTasks.stream().filter(t -> t.getCompletedAt() != null && t.getDueDate() != null &&
                t.getCompletedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isBefore(t.getDueDate().plusDays(1))).count();
        double onTimeRate = completed > 0 ? (onTime * 100.0 / completed) : 0;

        int highPriority = (int) completedTasks.stream().filter(t -> t.getPriority() == Priority.HIGH).count();
        int urgent = (int) completedTasks.stream().filter(t -> t.getPriority() == Priority.URGENT).count();

        return MemberPerformanceResponse.builder()
                .userId(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .totalTasksAssigned(total)
                .totalTasksCompleted(completed)
                .completionRate(Math.round(completionRate * 10) / 10.0)
                .tasksCompletedOnTime(onTime)
                .onTimeCompletionRate(Math.round(onTimeRate * 10) / 10.0)
                .highPriorityTasksCompleted(highPriority)
                .urgentTasksCompleted(urgent)
                .performanceTrend("STABLE")
                .build();
    }

    // ==================== MEMBER AVAILABILITY ====================

    public List<MemberAvailabilityResponse> getMemberAvailability(String managerEmail) {
        List<User> members = userRepository.findByRoleAndIsActiveTrue(RoleType.MEMBER);
        return members.stream()
                .map(this::calculateMemberAvailability)
                .sorted((a, b) -> Double.compare(a.getCapacityUtilization(), b.getCapacityUtilization()))
                .collect(Collectors.toList());
    }

    private MemberAvailabilityResponse calculateMemberAvailability(User member) {
        List<Task> activeTasks = taskRepository.findByAssignedToIdAndStatusIn(member.getId(), Arrays.asList(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
        int currentLoad = activeTasks.size();
        double utilization = (currentLoad * 100.0) / DEFAULT_CAPACITY;

        String status;
        if (utilization >= 100) status = "OVERLOADED";
        else if (utilization >= 75) status = "BUSY";
        else if (utilization >= 40) status = "MODERATE";
        else status = "AVAILABLE";

        LocalDateTime nextAvailable = activeTasks.stream().map(Task::getDueDate).filter(Objects::nonNull).map(LocalDate::atStartOfDay).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());

        return MemberAvailabilityResponse.builder()
                .userId(member.getId())
                .fullName(member.getFullName())
                .isAvailable(utilization < 75)
                .availabilityStatus(status)
                .currentTaskCount(currentLoad)
                .maxCapacity(DEFAULT_CAPACITY)
                .capacityUtilization(Math.round(utilization * 10) / 10.0)
                .nextAvailableDate(nextAvailable)
                .build();
    }

    // ==================== HELPERS ====================

    private TaskStatisticsResponse getTaskStatistics(List<Task> tasks) {
        Map<String, Integer> statusDist = new HashMap<>();
        statusDist.put("TODO", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count());
        statusDist.put("IN_PROGRESS", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count());
        statusDist.put("COMPLETED", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
        statusDist.put("CANCELLED", (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.CANCELLED).count());

        Map<String, Integer> priorityDist = new HashMap<>();
        priorityDist.put("LOW", (int) tasks.stream().filter(t -> t.getPriority() == Priority.LOW).count());
        priorityDist.put("MEDIUM", (int) tasks.stream().filter(t -> t.getPriority() == Priority.MEDIUM).count());
        priorityDist.put("HIGH", (int) tasks.stream().filter(t -> t.getPriority() == Priority.HIGH).count());
        priorityDist.put("URGENT", (int) tasks.stream().filter(t -> t.getPriority() == Priority.URGENT).count());

        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.plusDays(7);

        return TaskStatisticsResponse.builder()
                .totalTasks(tasks.size())
                .todoTasks(statusDist.get("TODO"))
                .inProgressTasks(statusDist.get("IN_PROGRESS"))
                .completedTasks(statusDist.get("COMPLETED"))
                .cancelledTasks(statusDist.get("CANCELLED"))
                .lowPriorityTasks(priorityDist.get("LOW"))
                .mediumPriorityTasks(priorityDist.get("MEDIUM"))
                .highPriorityTasks(priorityDist.get("HIGH"))
                .urgentTasks(priorityDist.get("URGENT"))
                .overdueTasks((int) tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != TaskStatus.COMPLETED).count())
                .dueTodayTasks((int) tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().equals(today)).count())
                .dueThisWeekTasks((int) tasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today) && t.getDueDate().isBefore(endOfWeek)).count())
                .statusDistribution(statusDist)
                .priorityDistribution(priorityDist)
                .build();
    }

// ==================== TASK PRIORITY ANALYSIS ====================

    public Map<String, Object> getTaskPriorityAnalysis(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        List<Project> projects = projectRepository.findByManagerUsername(manager.getEmail());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<Task> allTasks = projectIds.isEmpty() ? Collections.emptyList() : taskRepository.findAllByProjectIds(projectIds);

        Map<String, Object> analysis = new HashMap<>();

        // Priority distribution
        Map<String, Integer> priorityDist = new HashMap<>();
        priorityDist.put("LOW", (int) allTasks.stream().filter(t -> t.getPriority() == Priority.LOW && t.getStatus() != TaskStatus.COMPLETED).count());
        priorityDist.put("MEDIUM", (int) allTasks.stream().filter(t -> t.getPriority() == Priority.MEDIUM && t.getStatus() != TaskStatus.COMPLETED).count());
        priorityDist.put("HIGH", (int) allTasks.stream().filter(t -> t.getPriority() == Priority.HIGH && t.getStatus() != TaskStatus.COMPLETED).count());
        priorityDist.put("URGENT", (int) allTasks.stream().filter(t -> t.getPriority() == Priority.URGENT && t.getStatus() != TaskStatus.COMPLETED).count());

        analysis.put("priorityDistribution", priorityDist);
        analysis.put("totalActiveTasks", priorityDist.values().stream().mapToInt(Integer::intValue).sum());
        analysis.put("urgentTasksCount", priorityDist.get("URGENT"));
        analysis.put("highPriorityCount", priorityDist.get("HIGH"));

        return analysis;
    }

// ==================== TASK COMPLETION TREND (Last 7 Days) ====================

    public Map<String, Object> getTaskCompletionTrend(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        List<Project> projects = projectRepository.findByManagerUsername(manager.getEmail());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<Task> completedTasks = projectIds.isEmpty() ? Collections.emptyList() :
                taskRepository.findAllByProjectIds(projectIds).stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getCompletedAt() != null)
                        .collect(Collectors.toList());

        Map<String, Object> trend = new HashMap<>();
        Map<String, Integer> dailyCompletion = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = date.toString();

            int count = (int) completedTasks.stream()
                    .filter(t -> {
                        // Convert Instant to LocalDate using system default timezone
                        LocalDate completedDate = t.getCompletedAt()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        return completedDate.equals(date);
                    })
                    .count();

            dailyCompletion.put(dateKey, count);
        }

        trend.put("dailyCompletion", dailyCompletion);
        trend.put("weekTotal", dailyCompletion.values().stream().mapToInt(Integer::intValue).sum());
        trend.put("averagePerDay", dailyCompletion.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0));

        return trend;
    }

// ==================== UPCOMING DEADLINES ====================

    public List<Map<String, Object>> getUpcomingDeadlines(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        List<Project> projects = projectRepository.findByManagerUsername(manager.getEmail());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<Task> allTasks = projectIds.isEmpty() ? Collections.emptyList() : taskRepository.findAllByProjectIds(projectIds);

        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);

        return allTasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        t.getStatus() != TaskStatus.COMPLETED &&
                        t.getStatus() != TaskStatus.CANCELLED &&
                        !t.getDueDate().isBefore(today) &&
                        t.getDueDate().isBefore(weekFromNow))
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(10)
                .map(task -> {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("taskId", task.getId());
                    taskInfo.put("title", task.getTitle());
                    taskInfo.put("projectName", task.getProject().getProjectName());
                    taskInfo.put("assignedTo", task.getAssignedTo().getFullName());
                    taskInfo.put("dueDate", task.getDueDate());
                    taskInfo.put("priority", task.getPriority().toString());
                    taskInfo.put("status", task.getStatus().toString());
                    taskInfo.put("daysUntilDue", ChronoUnit.DAYS.between(today, task.getDueDate()));
                    return taskInfo;
                })
                .collect(Collectors.toList());
    }

// ==================== TOP PERFORMERS ====================

    public List<Map<String, Object>> getTopPerformers(String managerEmail) {
        List<MemberPerformanceResponse> performance = getMemberPerformance(managerEmail);

        return performance.stream()
                .limit(5)
                .map(member -> {
                    Map<String, Object> performer = new HashMap<>();
                    performer.put("userId", member.getUserId());
                    performer.put("fullName", member.getFullName());
                    performer.put("completionRate", member.getCompletionRate());
                    performer.put("totalCompleted", member.getTotalTasksCompleted());
                    performer.put("onTimeRate", member.getOnTimeCompletionRate());
                    return performer;
                })
                .collect(Collectors.toList());
    }

// ==================== TASK AGING ANALYSIS ====================

    public Map<String, Object> getTaskAgingAnalysis(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        List<Project> projects = projectRepository.findByManagerUsername(manager.getEmail());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<Task> activeTasks = projectIds.isEmpty() ? Collections.emptyList() :
                taskRepository.findAllByProjectIds(projectIds).stream()
                        .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                        .collect(Collectors.toList());

        LocalDate today = LocalDate.now();

        Map<String, Object> aging = new HashMap<>();

        int lessThan3Days = 0;
        int between3And7Days = 0;
        int between7And14Days = 0;
        int moreThan14Days = 0;

        for (Task task : activeTasks) {
            long daysOld = ChronoUnit.DAYS.between(task.getCreatedAt(), today);

            if (daysOld < 3) lessThan3Days++;
            else if (daysOld < 7) between3And7Days++;
            else if (daysOld < 14) between7And14Days++;
            else moreThan14Days++;
        }

        aging.put("lessThan3Days", lessThan3Days);
        aging.put("between3And7Days", between3And7Days);
        aging.put("between7And14Days", between7And14Days);
        aging.put("moreThan14Days", moreThan14Days);
        aging.put("totalActiveTasks", activeTasks.size());

        return aging;
    }

// ==================== PROJECT HEALTH SUMMARY ====================

    public Map<String, Object> getProjectHealthSummary(String managerEmail) {
        List<ProjectProgressResponse> projects = getProjectsProgress(managerEmail);

        Map<String, Object> summary = new HashMap<>();

        int onTrack = (int) projects.stream().filter(p -> p.getHealthStatus().equals("ON_TRACK")).count();
        int atRisk = (int) projects.stream().filter(p -> p.getHealthStatus().equals("AT_RISK")).count();
        int delayed = (int) projects.stream().filter(p -> p.getHealthStatus().equals("DELAYED")).count();
        int completed = (int) projects.stream().filter(p -> p.getHealthStatus().equals("COMPLETED")).count();

        summary.put("onTrack", onTrack);
        summary.put("atRisk", atRisk);
        summary.put("delayed", delayed);
        summary.put("completed", completed);
        summary.put("total", projects.size());

        // Calculate average progress
        double avgProgress = projects.stream()
                .filter(p -> !p.getHealthStatus().equals("COMPLETED"))
                .mapToDouble(ProjectProgressResponse::getProgressPercentage)
                .average()
                .orElse(0.0);

        summary.put("averageProgress", Math.round(avgProgress * 10) / 10.0);

        return summary;
    }

// ==================== TEAM CAPACITY ANALYSIS ====================

    public Map<String, Object> getTeamCapacityAnalysis(String managerEmail) {
        List<MemberWorkloadResponse> workloads = getMemberWorkloads(managerEmail);

        Map<String, Object> capacity = new HashMap<>();

        int overloaded = (int) workloads.stream().filter(w -> w.getWorkloadStatus().equals("OVERLOADED")).count();
        int high = (int) workloads.stream().filter(w -> w.getWorkloadStatus().equals("HIGH")).count();
        int medium = (int) workloads.stream().filter(w -> w.getWorkloadStatus().equals("MEDIUM")).count();
        int low = (int) workloads.stream().filter(w -> w.getWorkloadStatus().equals("LOW")).count();

        capacity.put("overloaded", overloaded);
        capacity.put("high", high);
        capacity.put("medium", medium);
        capacity.put("low", low);
        capacity.put("totalMembers", workloads.size());

        // Average workload percentage
        double avgWorkload = workloads.stream()
                .mapToDouble(MemberWorkloadResponse::getWorkloadPercentage)
                .average()
                .orElse(0.0);

        capacity.put("averageWorkload", Math.round(avgWorkload * 10) / 10.0);

        // Estimated hours
        int totalEstimatedHours = workloads.stream()
                .mapToInt(MemberWorkloadResponse::getEstimatedHoursRemaining)
                .sum();

        capacity.put("totalRemainingHours", totalEstimatedHours);

        return capacity;
    }
}