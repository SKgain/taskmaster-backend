package com.dhrubok.taskmaster.core.controllers.features.user;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.user.services.ManagerAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT_TOKEN;

@RestController
@RequestMapping("/api/manager/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manager Analytics", description = "Analytics and insights for managers")
@SecurityRequirement(name = JWT_TOKEN)
@PreAuthorize("hasRole('MANAGER')")
public class ManagerAnalyticsController {
    private final ManagerAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get complete dashboard overview")
    public ResponseEntity<Response> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Dashboard data retrieved",
                analyticsService.getDashboardOverview(authentication.getName())
        ));
    }

    @GetMapping("/workload")
    @Operation(summary = "Get team member workloads")
    public ResponseEntity<Response> getTeamWorkload(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Team workload retrieved",
                analyticsService.getMemberWorkloads(authentication.getName())
        ));
    }

    @GetMapping("/performance")
    @Operation(summary = "Get team member performance metrics")
    public ResponseEntity<Response> getTeamPerformance(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Performance metrics retrieved",
                analyticsService.getMemberPerformance(authentication.getName())
        ));
    }

    @GetMapping("/availability")
    @Operation(summary = "Get team member availability")
    public ResponseEntity<Response> getTeamAvailability(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Team availability retrieved",
                analyticsService.getMemberAvailability(authentication.getName())
        ));
    }

    @GetMapping("/projects/progress")
    @Operation(summary = "Get all projects progress")
    public ResponseEntity<Response> getProjectsProgress(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Projects progress retrieved",
                analyticsService.getProjectsProgress(authentication.getName())
        ));
    }

    @GetMapping("/tasks/distribution")
    @Operation(summary = "Get task distribution by status")
    public ResponseEntity<Response> getTaskStatusDistribution(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Task distribution retrieved",
                analyticsService.getTaskStatusDistribution(authentication.getName())
        ));
    }

    // ==================== NEW ADVANCED ANALYTICS ENDPOINTS ====================

    @GetMapping("/tasks/priority-analysis")
    @Operation(summary = "Get task priority distribution and analysis")
    public ResponseEntity<Response> getTaskPriorityAnalysis(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Priority analysis retrieved",
                analyticsService.getTaskPriorityAnalysis(authentication.getName())
        ));
    }

    @GetMapping("/tasks/completion-trend")
    @Operation(summary = "Get task completion trend for last 7 days")
    public ResponseEntity<Response> getTaskCompletionTrend(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Completion trend retrieved",
                analyticsService.getTaskCompletionTrend(authentication.getName())
        ));
    }

    @GetMapping("/tasks/upcoming-deadlines")
    @Operation(summary = "Get upcoming task deadlines (next 7 days)")
    public ResponseEntity<Response> getUpcomingDeadlines(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Upcoming deadlines retrieved",
                analyticsService.getUpcomingDeadlines(authentication.getName())
        ));
    }

    @GetMapping("/tasks/aging-analysis")
    @Operation(summary = "Get task aging analysis")
    public ResponseEntity<Response> getTaskAgingAnalysis(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Task aging analysis retrieved",
                analyticsService.getTaskAgingAnalysis(authentication.getName())
        ));
    }

    @GetMapping("/projects/health-summary")
    @Operation(summary = "Get project health summary")
    public ResponseEntity<Response> getProjectHealthSummary(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Project health summary retrieved",
                analyticsService.getProjectHealthSummary(authentication.getName())
        ));
    }

    @GetMapping("/team/capacity-analysis")
    @Operation(summary = "Get team capacity and workload analysis")
    public ResponseEntity<Response> getTeamCapacityAnalysis(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Team capacity analysis retrieved",
                analyticsService.getTeamCapacityAnalysis(authentication.getName())
        ));
    }

    @GetMapping("/team/top-performers")
    @Operation(summary = "Get top 5 performing team members")
    public ResponseEntity<Response> getTopPerformers(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Top performers retrieved",
                analyticsService.getTopPerformers(authentication.getName())
        ));
    }
}