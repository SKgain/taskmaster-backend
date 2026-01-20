package com.dhrubok.taskmaster.core.controllers.features.ai;

import com.dhrubok.taskmaster.common.models.Response;
import com.dhrubok.taskmaster.persistence.features.ai.services.AiTaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.dhrubok.taskmaster.auth.constants.SecurityConstant.JWT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@SecurityRequirement(name = JWT)
public class AiController {

    private final AiTaskService aiTaskService;

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