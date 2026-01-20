package com.dhrubok.taskmaster.persistence.features.ai.services;

import com.dhrubok.taskmaster.persistence.features.ai.models.GeminiRequest;
import com.dhrubok.taskmaster.persistence.features.ai.models.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskService {
    private final RestTemplate restTemplate;
    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;

    public String generateTaskDescription(String taskTitle) {

        String sanitizedTitle = taskTitle.trim()
                .replaceAll("[\\n\\r]", " ")
                .substring(0, Math.min(taskTitle.length(), 200));

        String finalUrl = apiUrl + "?key=" + apiKey;

        String systemPrompt = "You are an intelligent project management assistant. " +
                "Analyze the task title: '" + sanitizedTitle + "'. " +
                "Create a well-structured response with:\n" +
                "1. A brief professional description (2-3 sentences)\n" +
                "2. A clear list of 3-5 actionable subtasks\n\n" +
                "Format using clean HTML:\n" +
                "- Use <h4> for section headers\n" +
                "- Use <p> for paragraphs\n" +
                "- Use <ul> and <li> for lists\n" +
                "- Use <strong> for emphasis\n" +
                "- Keep it concise and professional\n" +
                "- Do NOT include <html>, <head>, <body>, or <div> tags\n" +
                "- Return only the formatted content";

        GeminiRequest request = new GeminiRequest(systemPrompt);

        try {
            GeminiResponse response = restTemplate.postForObject(finalUrl, request, GeminiResponse.class);

            if (response != null &&
                    response.getCandidates() != null &&
                    !response.getCandidates().isEmpty() &&
                    response.getCandidates().getFirst().getContent() != null &&
                    response.getCandidates().getFirst().getContent().getParts() != null &&
                    !response.getCandidates().getFirst().getContent().getParts().isEmpty()) {

                return response.getCandidates().getFirst().getContent().getParts().getFirst().getText();
            }

            return "AI could not generate a response.";

        } catch (Exception e) {

            return "Error connecting to AI service. Please try again later.";
        }
    }
}