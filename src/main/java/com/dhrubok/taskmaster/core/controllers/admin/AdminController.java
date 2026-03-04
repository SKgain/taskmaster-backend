package com.dhrubok.taskmaster.core.controllers.admin;

import com.dhrubok.taskmaster.common.annotations.ApiLog;
import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.admin.models.SystemStatsResponse;
import com.dhrubok.taskmaster.persistence.features.admin.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Operations", description = "Super Admin endpoints for system-wide management")
@SecurityRequirement(name = JWT)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get complete system statistics")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemStatsResponse.class)))
    @ApiLog
    @GetMapping("/stats")
    public ResponseEntity<Response> getSystemStats(Authentication authentication) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "System statistics retrieved successfully",
                        adminService.getSystemStats()
                )
        );
    }

    @Operation(summary = "Get system health status")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemStatsResponse.class)))
    @ApiLog
    @GetMapping("/health")
    public ResponseEntity<Response> getSystemHealth(Authentication authentication) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "System health retrieved",
                        adminService.getSystemHealth()
                )
        );
    }

    @Operation(summary = "Get all users with optional filters")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @GetMapping("/users")
    public ResponseEntity<Response> getAllUsers(Authentication authentication,
                                                @RequestParam(required = false) String role,
                                                @RequestParam(required = false) Boolean active) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Users retrieved successfully",
                        adminService.getAllUsers(role, active)
                )
        );
    }

    @Operation(summary = "Get user details by ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @GetMapping("/users/{userId}")
    public ResponseEntity<Response> getUserById(Authentication authentication,
                                                @PathVariable String userId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "User details retrieved",
                        adminService.getUserById(userId)
                )
        );
    }

    @Operation(summary = "Promote MEMBER to MANAGER")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<Response> promoteToManager(Authentication authentication,
                                                     @PathVariable String userId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "User promoted to MANAGER successfully",
                        adminService.promoteToManager(userId)
                )
        );
    }

    @Operation(summary = "Demote MANAGER to MEMBER")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @PutMapping("/users/{userId}/demote")
    public ResponseEntity<Response> demoteToMember(Authentication authentication,
                                                   @PathVariable String userId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Manager demoted to MEMBER successfully",
                        adminService.demoteToMember(userId)
                )
        );
    }

    @Operation(summary = "Deactivate user account")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<Response> deactivateUser(Authentication authentication,
                                                   @PathVariable String userId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "User deactivated successfully",
                        adminService.deactivateUser(userId)
                )
        );
    }

    @Operation(summary = "Activate user account")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<Response> activateUser(Authentication authentication,
                                                 @PathVariable String userId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "User activated successfully",
                        adminService.activateUser(userId)
                )
        );
    }

    @Operation(summary = "Send broadcast notification to users")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiLog
    @PostMapping("/notifications/broadcast")
    public ResponseEntity<Response> broadcastNotification(Authentication authentication,
                                                          @RequestParam String title,
                                                          @RequestParam String message,
                                                          @RequestParam(required = false) String role) throws MessagingException, IOException {

        adminService.broadcastNotification(title, message, role);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Broadcast notification sent successfully",
                        null
                )
        );
    }
}