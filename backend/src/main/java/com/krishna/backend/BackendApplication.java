package com.krishna.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Target Backend API.
 *
 * This is the application that the API Security Gateway is protecting.
 * It is intentionally simple — a realistic but minimal REST API
 * representing a typical downstream service.
 *
 * In the final architecture, clients should never call this directly;
 * all traffic passes through the gateway on port 8080.
 *
 * Port: 3000 (see application.properties)
 */
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
