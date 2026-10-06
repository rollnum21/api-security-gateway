package com.krishna.gateway.security;

import com.krishna.gateway.model.RequestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MetadataExtractor.
 * Verifies raw request metadata extraction, client IP resolution, and sensitive header filtering.
 */
class MetadataExtractorTest {

    private MetadataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new MetadataExtractor();
    }

    @Test
    void extractsBasicRequestFieldsAccurately() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.setQueryString("category=electronics&limit=10");
        request.setContentType("application/json");
        request.setRemoteAddr("192.168.1.50");

        RequestMetadata metadata = extractor.extract(request, null);

        assertNotNull(metadata.timestamp());
        assertEquals("GET", metadata.method());
        assertEquals("/api/products", metadata.path());
        assertEquals("category=electronics&limit=10", metadata.queryString());
        assertTrue(metadata.hasQueryString());
        assertEquals("application/json", metadata.contentType());
        assertEquals("192.168.1.50", metadata.clientIp());
        assertEquals(0, metadata.contentLength());
        assertFalse(metadata.hasBody());
    }

    @Test
    void extractsContentLengthAndBodyForPostRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/products");
        request.setContentType("application/json");
        byte[] body = "{\"name\":\"Monitor\",\"price\":299}".getBytes(StandardCharsets.UTF_8);

        RequestMetadata metadata = extractor.extract(request, body);

        assertEquals("POST", metadata.method());
        assertEquals(body.length, metadata.contentLength());
        assertTrue(metadata.hasBody());
        assertArrayEquals(body, metadata.body());
    }

    @Test
    void resolvesClientIpFromXForwardedForSingleAndMultipleHops() {
        // Single hop
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/users");
        req1.addHeader("X-Forwarded-For", "203.0.113.195");
        req1.setRemoteAddr("10.0.0.1");
        assertEquals("203.0.113.195", extractor.extractClientIp(req1));

        // Multiple hops: client, proxy1, proxy2
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/users");
        req2.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        req2.setRemoteAddr("10.0.0.1");
        assertEquals("203.0.113.195", extractor.extractClientIp(req2));

        // Fallback to X-Real-IP
        MockHttpServletRequest req3 = new MockHttpServletRequest("GET", "/api/users");
        req3.addHeader("X-Real-IP", "198.51.100.4");
        req3.setRemoteAddr("10.0.0.1");
        assertEquals("198.51.100.4", extractor.extractClientIp(req3));

        // Fallback to remoteAddr
        MockHttpServletRequest req4 = new MockHttpServletRequest("GET", "/api/users");
        req4.setRemoteAddr("127.0.0.1");
        assertEquals("127.0.0.1", extractor.extractClientIp(req4));
    }

    @Test
    void filtersOutSensitiveHeadersAndPreservesSafeHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.addHeader("Accept", "application/json");
        request.addHeader("X-Custom-Trace-Id", "trace-abc-123");

        // Sensitive credentials that must be stripped from captured metadata
        request.addHeader("Authorization", "Bearer super-secret-jwt-token");
        request.addHeader("Proxy-Authorization", "Basic dXNlcjpwYXNz");
        request.addHeader("Cookie", "sessionid=secretcookie123; user=alice");
        request.addHeader("Set-Cookie", "jwt=secret; Path=/");
        request.addHeader("X-Api-Key", "api-key-9999");
        request.addHeader("x-auth-token", "auth-token-8888");
        request.addHeader("X-Password-Hash", "hash123");
        request.addHeader("X-Secret-Access", "secretkey");

        RequestMetadata metadata = extractor.extract(request, null);

        // Safe headers must be preserved
        assertTrue(metadata.headers().containsKey("User-Agent"));
        assertEquals("Mozilla/5.0", metadata.headers().get("User-Agent"));
        assertTrue(metadata.headers().containsKey("Accept"));
        assertTrue(metadata.headers().containsKey("X-Custom-Trace-Id"));

        // Sensitive headers must NOT be captured
        assertFalse(metadata.headers().containsKey("Authorization"));
        assertFalse(metadata.headers().containsKey("Proxy-Authorization"));
        assertFalse(metadata.headers().containsKey("Cookie"));
        assertFalse(metadata.headers().containsKey("Set-Cookie"));
        assertFalse(metadata.headers().containsKey("X-Api-Key"));
        assertFalse(metadata.headers().containsKey("x-auth-token"));
        assertFalse(metadata.headers().containsKey("X-Password-Hash"));
        assertFalse(metadata.headers().containsKey("X-Secret-Access"));
    }

    @Test
    void toStringDoesNotLeakBodyOrSensitiveData() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        byte[] sensitiveBody = "{\"username\":\"admin\",\"password\":\"supersecret\"}".getBytes(StandardCharsets.UTF_8);

        RequestMetadata metadata = extractor.extract(request, sensitiveBody);
        String stringRep = metadata.toString();

        assertFalse(stringRep.contains("supersecret"));
        assertFalse(stringRep.contains("admin"));
        assertTrue(stringRep.contains("POST"));
        assertTrue(stringRep.contains("/api/login"));
    }
}
