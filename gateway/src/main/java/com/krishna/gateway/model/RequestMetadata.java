package com.krishna.gateway.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * RequestMetadata — Phase 6
 *
 * Immutable data transfer object representing the raw request metadata
 * captured at the gateway before security processing.
 *
 * Per Section 1 & Section 9 of the project plan:
 * This model contains strictly raw HTTP protocol attributes.
 * No feature engineering or ML transformations are applied here.
 */
public record RequestMetadata(
        Instant timestamp,
        String method,
        String path,
        String queryString,
        String contentType,
        long contentLength,
        String clientIp,
        Map<String, String> headers,
        byte[] body
) {
    public RequestMetadata {
        headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
        body = body != null ? body.clone() : new byte[0];
    }

    public boolean hasBody() {
        return body != null && body.length > 0;
    }

    public boolean hasQueryString() {
        return queryString != null && !queryString.isBlank();
    }

    @Override
    public String toString() {
        // Safe string representation: logs metadata structure without leaking raw payloads or credentials
        return "RequestMetadata[" +
                "timestamp=" + timestamp +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", hasQuery=" + hasQueryString() +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                ", clientIp='" + clientIp + '\'' +
                ", safeHeaderCount=" + headers.size() +
                ']';
    }
}
