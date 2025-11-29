package com.dhrubok.taskmaster.core.controllers.features.user;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.user.models.UpdateProfileRequest;
import com.dhrubok.taskmaster.persistence.features.user.models.UserResponse;
import com.dhrubok.taskmaster.persistence.features.user.services.UserUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Operations", description = "User profile and account management")
@SecurityRequirement(name = "JWT_TOKEN")
public class UserController {

    private final UserUserService userService;

    @Operation(summary = "Get current user profile", description = "Retrieve the profile of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/profile")
    public ResponseEntity<Response> getProfile(Authentication authentication) {

        log.info("GET /api/users/profile - User: {}", authentication.getName());

        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }

    @Operation(summary = "Update user profile", description = "Update the current user's profile information")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @PutMapping("/profile")
    public ResponseEntity<Response> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("PUT /api/users/profile - User: {} updating profile", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Profile updated successfully", userService.updateUserProfile(authentication.getName(), request))
        );
    }

    @Operation(summary = "Get user by ID", description = "Retrieve user profile by user ID")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<Response> getUserById(@PathVariable String id) {

        log.info("GET /api/users/{}", id);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "User retrieved successfully", userService.getUserById(id))
        );
    }

    @Operation(summary = "Search users", description = "Search for users by name or email (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<Response> searchUsers(@RequestParam String query) {

        log.info("GET /api/users/search?query={}", query);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Search results for: " + query, userService.searchUsers(query))
        );
    }

    @Operation(summary = "Get all active users", description = "Retrieve list of all active users in the system")
    @ApiResponse(responseCode = "200", description = "Active users retrieved", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/active")
    public ResponseEntity<Response> getAllActiveUsers() {

        log.info("GET /api/users/active");

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Active users retrieved", userService.getAllActiveUsers())
        );
    }
}