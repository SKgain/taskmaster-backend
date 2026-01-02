package com.dhrubok.taskmaster.persistence.features.ai.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(String prompt) {
        this.contents = Collections.singletonList(new Content(new Part(prompt)));
    }

    public void setContents(List<Content> contents) { this.contents = contents; }

    // Inner classes for the nested JSON structure
    public static class Content {
        private List<Part> parts;
        public Content(Part part) { this.parts = Collections.singletonList(part); }
        public List<Part> getParts() { return parts; }
    }

    public static class Part {
        private String text;
        public Part(String text) { this.text = text; }
        public String getText() { return text; }
    }
}