package com.dhrubok.taskmaster.core.controllers.admin;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.system.entities.SystemConfig;
import com.dhrubok.taskmaster.persistence.system.models.SystemConfigDTO;
import com.dhrubok.taskmaster.persistence.system.services.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Tag(name = "Admin System Config", description = "Manage global system settings")
@Slf4j
@SecurityRequirement(name = JWT)
public class AdminConfigController {

    private final SystemConfigService configService;

    @Operation(summary = "Get current system configuration")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemConfig.class)))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getSystemConfig(Authentication authentication) {
        try {
            SystemConfigDTO config = configService.getCurrentConfiguration();

            return ResponseEntity.ok(
                    Response.getResponseEntity(
                            true,
                            "System configuration loaded successfully",
                            config
                    )
            );
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.getResponseEntity(
                            false,
                            "Error loading configuration: " + e.getMessage(),
                            null
                    ));
        }
    }

    @Operation(summary = "Update system configuration")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemConfig.class)))
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateSystemConfig(Authentication authentication,
                                                       @Valid @RequestBody SystemConfigDTO configData) {
        try {
            SystemConfigDTO updatedConfig = configService.updateConfiguration(configData);

            return ResponseEntity.ok(
                    Response.getResponseEntity(
                            true,
                            "System configuration updated successfully",
                            updatedConfig
                    )
            );
        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Response.getResponseEntity(
                            false,
                            "Invalid configuration: " + e.getMessage(),
                            null
                    ));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.getResponseEntity(
                            false,
                            "Error updating configuration: " + e.getMessage(),
                            null
                    ));
        }
    }

    @Operation(summary = "Reset system configuration to defaults")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SystemConfig.class)))
    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> resetSystemConfig(Authentication authentication) {

        try {
            SystemConfigDTO defaultConfig = configService.resetToDefaults();

            return ResponseEntity.ok(
                    Response.getResponseEntity(
                            true,
                            "System configuration reset to defaults",
                            defaultConfig
                    )
            );
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.getResponseEntity(
                            false,
                            "Error resetting configuration: " + e.getMessage(),
                            null
                    ));
        }
    }
}