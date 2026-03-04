package com.dhrubok.taskmaster.core.controllers.features.ai;

import com.dhrubok.taskmaster.auth.constants.SecurityConstant;
import com.dhrubok.taskmaster.common.annotations.ApiLog;
import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.ai.services.AiTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequiredArgsConstructor
@Tag(name = "AI ", description = "Ai related operations.")
@RequestMapping("/api/ai")
@SecurityRequirement(name = JWT)
public class AiController {

    private final AiTaskService aiTaskService;

    @Operation(summary = "Ai task description generate", security = @SecurityRequirement(name = SecurityConstant.JWT))
    @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class)), responseCode = "200")
    @ApiLog
    @PostMapping("/generate")
    public ResponseEntity<Response> generateSuggestion(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.ok(
                    Response.getResponseEntity(
                            false,
                            "Task title is required",
                            null
                    )
            );
        }

        String aiResponse = aiTaskService.generateTaskDescription(title);

        return ResponseEntity.ok(
                Response.getResponseEntity(
                        true,
                        "Suggestion generated",
                        Map.of("suggestion", aiResponse))
                );
    }
}