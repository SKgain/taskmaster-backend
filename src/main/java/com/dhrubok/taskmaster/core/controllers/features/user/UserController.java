package com.dhrubok.taskmaster.core.controllers.features.user;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.auth.models.ChangePasswordRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT_TOKEN;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Operations", description = "User profile and account management")
@SecurityRequirement(name = JWT_TOKEN)
public class UserController {

    private final UserUserService userService;

    @Operation(summary = "Get current user profile")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/profile")
    public ResponseEntity<Response> getProfile(Authentication authentication) {
        log.info("GET /api/users/profile - User: {}", authentication.getName());
        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }

    @Operation(summary = "Update user profile")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @PutMapping("/profile")
    public ResponseEntity<Response> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("PUT /api/users/profile - User: {} updating profile", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Profile updated successfully",
                        userService.updateUserProfile(authentication.getName(), request)
                )
        );
    }

    @Operation(summary = "Upload profile photo")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PostMapping("/profile/photo")
    public ResponseEntity<Response> uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("POST /api/users/profile/photo - User: {} uploading photo", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Profile photo uploaded successfully",
                        userService.uploadProfilePhoto(authentication.getName(), file)
                )
        );
    }

    @Operation(summary = "Change password")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PostMapping("/change-password")
    public ResponseEntity<Response> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("POST /api/users/change-password - User: {}", authentication.getName());

        userService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Password changed successfully", null)
        );
    }

    // ========================================
    // 🆕 NEW ENDPOINT - GET ALL USERS
    // ========================================
    @Operation(summary = "Get all users (for project member assignment)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping
    public ResponseEntity<Response> getAllUsers(Authentication authentication) {
        log.info("GET /api/users - Requested by: {}", authentication.getName());

        try {
            return ResponseEntity.ok(
                    Response.getResponseEntity(
                            true,
                            "Users retrieved successfully",
                            userService.getAllActiveUsers()
                    )
            );
        } catch (Exception e) {
            log.error("Error retrieving all users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    Response.getResponseEntity(
                            false,
                            "Failed to retrieve users: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<Response> getUserById(@PathVariable String id) {
        log.info("GET /api/users/{}", id);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "User retrieved successfully", userService.getUserById(id))
        );
    }

    @Operation(summary = "Search users")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<Response> searchUsers(@RequestParam String query) {
        log.info("GET /api/users/search?query={}", query);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Search results for: " + query, userService.searchUsers(query))
        );
    }

    @Operation(summary = "Get all active users")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/active")
    public ResponseEntity<Response> getAllActiveUsers() {
        log.info("GET /api/users/active");

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Active users retrieved", userService.getAllActiveUsers())
        );
    }
}