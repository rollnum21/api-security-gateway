package com.krishna.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HealthController — Phase 3
 *
 * Provides a simple liveness check for the API Security Gateway.
 * This endpoint is the first thing tested after startup and proves
 * that Spring Boot is running correctly before any proxy or ML
 * logic is added.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     *
     * Returns a JSON object confirming the gateway is alive.
     *
     * Example response:
     * {
     *   "status": "UP",
     *   "service": "API Security Gateway"
     * }
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        // LinkedHashMap preserves insertion order in JSON output
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "API Security Gateway");
        return ResponseEntity.ok(response);
    }
}
