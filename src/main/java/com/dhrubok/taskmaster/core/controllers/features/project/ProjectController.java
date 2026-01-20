package com.dhrubok.taskmaster.core.controllers.features.project;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.project.models.CreateProjectRequest;
import com.dhrubok.taskmaster.persistence.features.project.models.UpdateProjectRequest;
import com.dhrubok.taskmaster.persistence.features.project.services.ProjectService;
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

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "Project CRUD operations")
@SecurityRequirement(name = JWT)
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create a new project (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<Response> createProject(Authentication authentication,
                                                  @Valid @RequestBody CreateProjectRequest request) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project created successfully",
                        projectService.createProject(authentication.getName(), request)
                )
        );
    }

    @Operation(summary = "Get all projects for current user")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping
    public ResponseEntity<Response> getAllProjects(Authentication authentication) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Projects retrieved successfully",
                        projectService.getAllProjectsForUser(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get project by ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/{projectId}")
    public ResponseEntity<Response> getProjectById(Authentication authentication,
                                                   @PathVariable String projectId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project retrieved successfully",
                        projectService.getProjectById(authentication.getName(), projectId)
                )
        );
    }

    @Operation(summary = "Update project (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{projectId}")
    public ResponseEntity<Response> updateProject(Authentication authentication,
                                                  @PathVariable String projectId,
                                                  @Valid @RequestBody UpdateProjectRequest request) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project updated successfully",
                        projectService.updateProject(authentication.getName(), projectId, request)
                )
        );
    }

    @Operation(summary = "Archive project (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{projectId}/archive")
    public ResponseEntity<Response> archiveProject(Authentication authentication,
                                                   @PathVariable String projectId) {

        projectService.archiveProject(authentication.getName(), projectId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project archived successfully",
                        null)
        );
    }

    @Operation(summary = "Delete project (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Response> deleteProject(Authentication authentication,
                                                  @PathVariable String projectId) {

        projectService.deleteProject(authentication.getName(), projectId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project deleted successfully",
                        null)
        );
    }

    @Operation(summary = "Add member to project (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Response> addMemberToProject(Authentication authentication,
                                                       @PathVariable String projectId,
                                                       @PathVariable String memberId) {

        projectService.addMemberToProject(authentication.getName(), projectId, memberId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member added to project successfully",
                        null)
        );
    }

    @Operation(summary = "Remove member from project (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Response> removeMemberFromProject(Authentication authentication,
                                                            @PathVariable String projectId,
                                                            @PathVariable String memberId) {

        projectService.removeMemberFromProject(authentication.getName(), projectId, memberId);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Member removed from project successfully",
                        null)
        );
    }

    @Operation(summary = "Get all members of a project")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/{projectId}/members")
    public ResponseEntity<Response> getProjectMembers(Authentication authentication,
                                                      @PathVariable String projectId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project members retrieved successfully",
                        projectService.getProjectMembers(authentication.getName(), projectId)
                )
        );
    }

    @Operation(summary = "Get project statistics (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{projectId}/stats")
    public ResponseEntity<Response> getProjectStats(Authentication authentication,
                                                    @PathVariable String projectId) {

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project statistics retrieved successfully",
                        projectService.getProjectStats(authentication.getName(), projectId)
                )
        );
    }
}