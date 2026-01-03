package com.dhrubok.taskmaster.core.controllers.features.meeting;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.meeting.models.CreateMeetingRequest;
import com.dhrubok.taskmaster.persistence.features.meeting.models.UpdateMeetingRequest;
import com.dhrubok.taskmaster.persistence.features.meeting.services.MeetingService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT_TOKEN;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meeting Management", description = "Meeting scheduling and management operations")
@SecurityRequirement(name = JWT_TOKEN)
public class MeetingController {

    private final MeetingService meetingService;

    @Operation(summary = "Create a new meeting (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<Response> createMeeting(
            Authentication authentication,
            @Valid @RequestBody CreateMeetingRequest request) {

        log.info("POST /api/meetings - Manager: {} creating meeting", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Meeting created successfully",
                        meetingService.createMeeting(authentication.getName(), request)
                )
        );
    }

    @Operation(summary = "Get all meetings for current user")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping
    public ResponseEntity<Response> getAllMeetings(Authentication authentication) {

        log.info("GET /api/meetings - User: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Meetings retrieved successfully",
                        meetingService.getAllMeetingsForUser(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get meetings by project")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Response> getMeetingsByProject(
            Authentication authentication,
            @PathVariable String projectId) {

        log.info("GET /api/meetings/project/{} - User: {}", projectId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project meetings retrieved successfully",
                        meetingService.getMeetingsByProject(authentication.getName(), projectId)
                )
        );
    }

    @Operation(summary = "Get meeting by ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/{meetingId}")
    public ResponseEntity<Response> getMeetingById(
            Authentication authentication,
            @PathVariable String meetingId) {

        log.info("GET /api/meetings/{} - User: {}", meetingId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Meeting retrieved successfully",
                        meetingService.getMeetingById(authentication.getName(), meetingId)
                )
        );
    }

    @Operation(summary = "Update meeting (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{meetingId}")
    public ResponseEntity<Response> updateMeeting(
            Authentication authentication,
            @PathVariable String meetingId,
            @Valid @RequestBody UpdateMeetingRequest request) {

        log.info("PUT /api/meetings/{} - Manager: {}", meetingId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Meeting updated successfully",
                        meetingService.updateMeeting(authentication.getName(), meetingId, request)
                )
        );
    }

    @Operation(summary = "Cancel meeting (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{meetingId}/cancel")
    public ResponseEntity<Response> cancelMeeting(
            Authentication authentication,
            @PathVariable String meetingId) {

        log.info("PATCH /api/meetings/{}/cancel - Manager: {}", meetingId, authentication.getName());

        meetingService.cancelMeeting(authentication.getName(), meetingId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Meeting cancelled successfully", null)
        );
    }

    @Operation(summary = "Mark meeting as completed (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{meetingId}/complete")
    public ResponseEntity<Response> completeMeeting(
            Authentication authentication,
            @PathVariable String meetingId) {

        log.info("PATCH /api/meetings/{}/complete - Manager: {}", meetingId, authentication.getName());

        meetingService.completeMeeting(authentication.getName(), meetingId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Meeting marked as completed", null)
        );
    }

    @Operation(summary = "Add participant to meeting (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{meetingId}/participants/{userId}")
    public ResponseEntity<Response> addParticipant(
            Authentication authentication,
            @PathVariable String meetingId,
            @PathVariable String userId) {

        log.info("POST /api/meetings/{}/participants/{} - Manager: {}",
                meetingId, userId, authentication.getName());

        meetingService.addParticipant(authentication.getName(), meetingId, userId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Participant added successfully", null)
        );
    }

    @Operation(summary = "Remove participant from meeting (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{meetingId}/participants/{userId}")
    public ResponseEntity<Response> removeParticipant(
            Authentication authentication,
            @PathVariable String meetingId,
            @PathVariable String userId) {

        log.info("DELETE /api/meetings/{}/participants/{} - Manager: {}",
                meetingId, userId, authentication.getName());

        meetingService.removeParticipant(authentication.getName(), meetingId, userId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Participant removed successfully", null)
        );
    }

    @Operation(summary = "Delete meeting (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Response> deleteMeeting(
            Authentication authentication,
            @PathVariable String meetingId) {

        log.info("DELETE /api/meetings/{} - Manager: {}", meetingId, authentication.getName());

        meetingService.deleteMeeting(authentication.getName(), meetingId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Meeting deleted successfully", null)
        );
    }

    @Operation(summary = "Get upcoming meetings for current user")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/upcoming")
    public ResponseEntity<Response> getUpcomingMeetings(Authentication authentication) {

        log.info("GET /api/meetings/upcoming - User: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Upcoming meetings retrieved successfully",
                        meetingService.getUpcomingMeetings(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get past meetings for current user")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/past")
    public ResponseEntity<Response> getPastMeetings(Authentication authentication) {

        log.info("GET /api/meetings/past - User: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Past meetings retrieved successfully",
                        meetingService.getPastMeetings(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get meeting statistics (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/stats")
    public ResponseEntity<Response> getMeetingStats(Authentication authentication) {

        log.info("GET /api/meetings/stats - Manager: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Meeting statistics retrieved successfully",
                        meetingService.getMeetingStats(authentication.getName())
                )
        );
    }

    @Operation(summary = "Search meetings by title or description")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/search")
    public ResponseEntity<Response> searchMeetings(
            Authentication authentication,
            @RequestParam String query) {

        log.info("GET /api/meetings/search?query={} - User: {}", query, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Search results for: " + query,
                        meetingService.searchMeetings(authentication.getName(), query)
                )
        );
    }
}