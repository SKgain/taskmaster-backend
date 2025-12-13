package com.dhrubok.taskmaster.core.controllers.features.user;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.user.models.UpdateSettingsRequest;
import com.dhrubok.taskmaster.persistence.features.user.models.UserSettingsResponse;
import com.dhrubok.taskmaster.persistence.features.user.services.UserSettingsService;
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

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT_TOKEN;

@RestController
@RequestMapping("/api/users/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Settings", description = "User notification and preference settings")
@SecurityRequirement(name = JWT_TOKEN)
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @Operation(summary = "Get user settings")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserSettingsResponse.class)))
    @GetMapping
    public ResponseEntity<Response> getSettings(Authentication authentication) {
        log.info("GET /api/users/settings - User: {}", authentication.getName());

        UserSettingsResponse settings = userSettingsService.getUserSettings(authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Settings retrieved successfully", settings)
        );
    }

    @Operation(summary = "Update user settings")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserSettingsResponse.class)))
    @PutMapping
    public ResponseEntity<Response> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UpdateSettingsRequest request) {

        log.info("PUT /api/users/settings - User: {} updating settings", authentication.getName());

        UserSettingsResponse settings = userSettingsService.updateUserSettings(authentication.getName(), request);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Settings updated successfully", settings)
        );
    }

    @Operation(summary = "Reset settings to default")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserSettingsResponse.class)))
    @PostMapping("/reset")
    public ResponseEntity<Response> resetSettings(Authentication authentication) {
        log.info("POST /api/users/settings/reset - User: {}", authentication.getName());

        UserSettingsResponse settings = userSettingsService.resetToDefault(authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Settings reset to default successfully", settings)
        );
    }
}