package com.dhrubok.taskmaster.core.controllers.features.task;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.task.models.CreateTaskRequest;
import com.dhrubok.taskmaster.persistence.features.task.models.UpdateTaskRequest;
import com.dhrubok.taskmaster.persistence.features.task.models.UpdateTaskStatusRequest;
import com.dhrubok.taskmaster.persistence.features.task.services.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Management", description = "Task CRUD and assignment operations")
@SecurityRequirement(name = JWT_TOKEN)
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create a new task (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<Response> createTask(
            Authentication authentication,
            @Valid @RequestBody CreateTaskRequest request) {

        log.info("POST /api/tasks - Manager: {} creating task", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Task created and assigned successfully",
                        taskService.createTask(authentication.getName(), request)
                )
        );
    }

    @Operation(summary = "Get all tasks for current user")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping
    public ResponseEntity<Response> getAllTasks(Authentication authentication) {

        log.info("GET /api/tasks - User: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Tasks retrieved successfully",
                        taskService.getAllTasksForUser(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get tasks by project")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Response> getTasksByProject(
            Authentication authentication,
            @PathVariable String projectId) {

        log.info("GET /api/tasks/project/{} - User: {}", projectId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Project tasks retrieved successfully",
                        taskService.getTasksByProject(authentication.getName(), projectId)
                )
        );
    }

    @Operation(summary = "Get task by ID")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/{taskId}")
    public ResponseEntity<Response> getTaskById(
            Authentication authentication,
            @PathVariable String taskId) {

        log.info("GET /api/tasks/{} - User: {}", taskId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Task retrieved successfully",
                        taskService.getTaskById(authentication.getName(), taskId)
                )
        );
    }

    @Operation(summary = "Update task (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{taskId}")
    public ResponseEntity<Response> updateTask(
            Authentication authentication,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        log.info("PUT /api/tasks/{} - Manager: {}", taskId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Task updated successfully",
                        taskService.updateTask(authentication.getName(), taskId, request)
                )
        );
    }

    @Operation(summary = "Update task status (Member can update their own tasks)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Response> updateTaskStatus(
            Authentication authentication,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {

        log.info("PATCH /api/tasks/{}/status - User: {}", taskId, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Task status updated successfully",
                        taskService.updateTaskStatus(authentication.getName(), taskId, request)
                )
        );
    }

    @Operation(summary = "Reassign task to another member (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{taskId}/assign/{memberId}")
    public ResponseEntity<Response> reassignTask(
            Authentication authentication,
            @PathVariable String taskId,
            @PathVariable String memberId) {

        log.info("PATCH /api/tasks/{}/assign/{} - Manager: {}",
                taskId, memberId, authentication.getName());

        taskService.reassignTask(authentication.getName(), taskId, memberId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Task reassigned successfully", null)
        );
    }

    @Operation(summary = "Delete task (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Response> deleteTask(
            Authentication authentication,
            @PathVariable String taskId) {

        log.info("DELETE /api/tasks/{} - Manager: {}", taskId, authentication.getName());

        taskService.deleteTask(authentication.getName(), taskId);

        return ResponseEntity.ok(
                Response.getResponseEntity(true, "Task deleted successfully", null)
        );
    }

    @Operation(summary = "Get tasks assigned to current user (Member/Manager)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/my-tasks")
    public ResponseEntity<Response> getMyTasks(Authentication authentication) {

        log.info("GET /api/tasks/my-tasks - User: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "My tasks retrieved successfully",
                        taskService.getTasksAssignedToUser(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get overdue tasks (Manager sees all, Member sees own)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/overdue")
    public ResponseEntity<Response> getOverdueTasks(Authentication authentication) {

        log.info("GET /api/tasks/overdue - User: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Overdue tasks retrieved successfully",
                        taskService.getOverdueTasks(authentication.getName())
                )
        );
    }

    @Operation(summary = "Get task statistics (Manager only)")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/stats")
    public ResponseEntity<Response> getTaskStats(Authentication authentication) {

        log.info("GET /api/tasks/stats - Manager: {}", authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Task statistics retrieved successfully",
                        taskService.getTaskStats(authentication.getName())
                )
        );
    }

    @Operation(summary = "Search tasks by title or description")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    @GetMapping("/search")
    public ResponseEntity<Response> searchTasks(
            Authentication authentication,
            @RequestParam String query) {

        log.info("GET /api/tasks/search?query={} - User: {}", query, authentication.getName());

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Search results for: " + query,
                        taskService.searchTasks(authentication.getName(), query)
                )
        );
    }
}