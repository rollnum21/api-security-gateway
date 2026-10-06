package com.krishna.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BackendController — Phase 4
 *
 * Provides three simple endpoints that represent the downstream API
 * being protected by the security gateway.
 *
 * This class is intentionally simple. Its only purpose is to give
 * the gateway something realistic to proxy to and to let us verify
 * that the gateway correctly forwards requests and returns responses.
 *
 * In the final architecture:
 *   Clients → Gateway (:8080) → Backend (:3000)
 * Clients should never call port 3000 directly in production.
 */
@RestController
@RequestMapping("/api")
public class BackendController {

    // ─── Health ─────────────────────────────────────────────────────

    /**
     * GET /api/backend/health
     *
     * Liveness check for the backend service.
     * The gateway uses this path pattern to confirm the backend is up.
     *
     * Response: { "status": "UP", "service": "Target Backend API" }
     */
    @GetMapping("/backend/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "Target Backend API");
        return ResponseEntity.ok(response);
    }

    // ─── Products ───────────────────────────────────────────────────

    /**
     * GET /api/products
     *
     * Returns a static list of product names representing a typical
     * catalogue endpoint that the gateway will protect.
     *
     * In the demo, a normal request to this path via the gateway
     * should be scored NORMAL by the ML service and forwarded here.
     * A request with an attack payload in the query string should be
     * scored ANOMALY and blocked with HTTP 403 before reaching here.
     *
     * Response: ["Laptop", "Keyboard", "Mouse", "Monitor", "Webcam"]
     */
    @GetMapping("/products")
    public ResponseEntity<List<String>> products() {
        List<String> productList = List.of(
                "Laptop",
                "Keyboard",
                "Mouse",
                "Monitor",
                "Webcam"
        );
        return ResponseEntity.ok(productList);
    }

    /**
     * POST /api/products
     *
     * Accepts a product payload, confirming that mutating requests with request bodies
     * are properly handled and forwarded by the security gateway.
     */
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody(required = false) Map<String, Object> product) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "CREATED");
        response.put("product", product != null ? product : Collections.emptyMap());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── Users ──────────────────────────────────────────────────────

    /**
     * GET /api/users
     *
     * Returns a static list of user records representing a typical
     * user-management endpoint. This is a realistic target for
     * SQL-injection and path-traversal attack attempts, which the
     * gateway's anomaly detector is designed to catch.
     *
     * Response: [ { "id": 1, "name": "Alice", "role": "admin" }, ... ]
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> users() {
        List<Map<String, Object>> userList = List.of(
                userRecord(1, "Alice",   "admin"),
                userRecord(2, "Bob",     "user"),
                userRecord(3, "Charlie", "user"),
                userRecord(4, "Diana",   "moderator")
        );
        return ResponseEntity.ok(userList);
    }

    // ─── Helper ─────────────────────────────────────────────────────

    private Map<String, Object> userRecord(int id, String name, String role) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id",   id);
        user.put("name", name);
        user.put("role", role);
        return user;
    }
}
