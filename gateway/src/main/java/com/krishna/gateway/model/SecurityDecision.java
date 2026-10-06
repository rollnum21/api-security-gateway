package com.krishna.gateway.model;

import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

/**
 * SecurityDecision — Phase 6
 *
 * Represents the allow/block decision produced by the security pipeline.
 * In Phase 6, all decisions default to ALLOW. In Phase 14, this will carry
 * decisions computed by the ML model and signature detector.
 */
public class SecurityDecision {

    public enum Action {
        ALLOW,
        BLOCK
    }

    private final Action action;
    private final int statusCode;
    private final String reason;

    public SecurityDecision(Action action, int statusCode, String reason) {
        this.action = action;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public static SecurityDecision allow(String reason) {
        return new SecurityDecision(Action.ALLOW, HttpStatus.OK.value(), reason);
    }

    public static SecurityDecision block(int statusCode, String reason) {
        return new SecurityDecision(Action.BLOCK, statusCode, reason);
    }

    public static SecurityDecision block(String reason) {
        return block(HttpStatus.FORBIDDEN.value(), reason);
    }

    public Action getAction() {
        return action;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }

    public boolean isAllowed() {
        return action == Action.ALLOW;
    }

    public boolean isBlocked() {
        return action == Action.BLOCK;
    }

    public byte[] toJsonBytes() {
        String safeReason = reason != null ? reason.replace("\"", "\\\"") : "Request blocked by security policy";
        String json = String.format("{\"error\":\"Forbidden\",\"message\":\"%s\"}", safeReason);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "SecurityDecision{" +
                "action=" + action +
                ", statusCode=" + statusCode +
                ", reason='" + reason + '\'' +
                '}';
    }
}
