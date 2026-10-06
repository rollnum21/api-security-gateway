package com.krishna.gateway.security;

import com.krishna.gateway.model.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * MetadataExtractor — Phase 6
 *
 * Extracts security-relevant raw request attributes from an incoming HTTP servlet request
 * and produces a clean, immutable RequestMetadata DTO.
 *
 * Security precautions:
 * - Sensitive headers (Authorization, Cookie, API Keys, Tokens, Passwords) are filtered out.
 * - Source IP is extracted resolving upstream proxy headers (X-Forwarded-For, X-Real-IP).
 * - Raw request body is captured without stream mutation.
 */
@Component
public class MetadataExtractor {

    /**
     * Exact lowercase names of sensitive headers that must never be captured or logged.
     */
    private static final Set<String> SENSITIVE_HEADER_NAMES = Set.of(
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "x-api-key",
            "api-key",
            "x-auth-token"
    );

    /**
     * Substrings that indicate sensitive credentials in header names.
     */
    private static final Set<String> SENSITIVE_KEYWORD_SUBSTRINGS = Set.of(
            "password",
            "secret",
            "token",
            "credential"
    );

    /**
     * Extracts raw request metadata from the incoming servlet request and optional body.
     *
     * @param request the incoming HttpServletRequest
     * @param body    the raw request body bytes (can be null)
     * @return the immutable RequestMetadata
     */
    public RequestMetadata extract(HttpServletRequest request, byte[] body) {
        Instant timestamp = Instant.now();
        String method = request.getMethod() != null ? request.getMethod().toUpperCase(Locale.ROOT) : "GET";
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String contentType = request.getContentType();

        byte[] safeBody = body != null ? body : new byte[0];
        long contentLength = safeBody.length > 0 ? safeBody.length : Math.max(0, request.getContentLengthLong());

        String clientIp = extractClientIp(request);
        Map<String, String> safeHeaders = extractSafeHeaders(request);

        return new RequestMetadata(
                timestamp,
                method,
                path,
                queryString,
                contentType,
                contentLength,
                clientIp,
                safeHeaders,
                safeBody
        );
    }

    /**
     * Determines the client IP address, checking X-Forwarded-For, X-Real-IP,
     * and falling back to getRemoteAddr().
     */
    public String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For can be a comma-separated list of IPs: "client, proxy1, proxy2"
            int commaIndex = xForwardedFor.indexOf(',');
            if (commaIndex > -1) {
                return xForwardedFor.substring(0, commaIndex).trim();
            }
            return xForwardedFor.trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    /**
     * Captures request headers while excluding sensitive authentication credentials,
     * tokens, passwords, and cookies.
     */
    public Map<String, String> extractSafeHeaders(HttpServletRequest request) {
        Map<String, String> safeHeaders = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames == null) {
            return Collections.emptyMap();
        }

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName == null || isSensitiveHeader(headerName)) {
                continue;
            }
            String value = request.getHeader(headerName);
            if (value != null) {
                safeHeaders.put(headerName, value);
            }
        }

        return Collections.unmodifiableMap(safeHeaders);
    }

    /**
     * Returns true if the header name represents sensitive credentials that must not be stored or logged.
     */
    public boolean isSensitiveHeader(String headerName) {
        if (headerName == null) {
            return true;
        }
        String lower = headerName.toLowerCase(Locale.ROOT);
        if (SENSITIVE_HEADER_NAMES.contains(lower)) {
            return true;
        }
        for (String keyword : SENSITIVE_KEYWORD_SUBSTRINGS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
