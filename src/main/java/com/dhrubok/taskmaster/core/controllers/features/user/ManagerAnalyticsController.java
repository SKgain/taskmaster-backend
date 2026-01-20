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

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequestMapping("/api/manager/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manager Analytics", description = "Analytics and insights for managers")
@SecurityRequirement(name = JWT)
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

    @GetMapping("/projects/progress")
    @Operation(summary = "Get all projects progress")
    public ResponseEntity<Response> getProjectsProgress(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Projects progress retrieved",
                analyticsService.getProjectsProgress(authentication.getName())
        ));
    }

    @GetMapping("/tasks/priority-analysis")
    @Operation(summary = "Get task priority distribution and analysis")
    public ResponseEntity<Response> getTaskPriorityAnalysis(Authentication authentication) {
        return ResponseEntity.ok(Response.getResponseEntity(
                true,
                "Priority analysis retrieved",
                analyticsService.getTaskPriorityAnalysis(authentication.getName())
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
}