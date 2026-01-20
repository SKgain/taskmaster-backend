package com.dhrubok.taskmaster.persistence.features.admin.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponse {
    private String status; // HEALTHY, WARNING, CRITICAL
    private Long uptime;
    private String version;
    private Map<String, String> services;
    private Map<String, Object> metrics;
}