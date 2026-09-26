package com.krishna.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API Security Gateway.
 *
 * The gateway sits between clients and the target backend API.
 * It intercepts every incoming HTTP request, sends request metadata
 * to the ML anomaly-detection service, and either forwards or blocks
 * the request based on the prediction.
 *
 * Current phase: Phase 3 skeleton — health endpoint only.
 * Proxy, ML integration, and security decision engine will be added
 * in later phases.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
