package com.dhrubok.taskmaster.persistence.features.ai.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GeminiResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() { return candidates; }
    public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }

    public static class Candidate {
        private Content content;
        public Content getContent() { return content; }
    }

    public static class Content {
        private List<Part> parts;
        public List<Part> getParts() { return parts; }
    }

    public static class Part {
        private String text;
        public String getText() { return text; }
    }
}