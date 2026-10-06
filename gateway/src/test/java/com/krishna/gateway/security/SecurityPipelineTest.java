package com.krishna.gateway.security;

import com.krishna.gateway.model.RequestMetadata;
import com.krishna.gateway.model.SecurityDecision;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityPipeline and SecurityDecision.
 */
class SecurityPipelineTest {

    @Test
    void defaultPipelineReturnsAllow() {
        MetadataExtractor extractor = new MetadataExtractor();
        DefaultSecurityPipeline pipeline = new DefaultSecurityPipeline();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        RequestMetadata metadata = extractor.extract(request, null);

        SecurityDecision decision = pipeline.evaluate(metadata);

        assertNotNull(decision);
        assertTrue(decision.isAllowed());
        assertFalse(decision.isBlocked());
        assertEquals(SecurityDecision.Action.ALLOW, decision.getAction());
        assertEquals(200, decision.getStatusCode());
        assertTrue(decision.getReason().contains("Phase 6 baseline"));
    }

    @Test
    void blockDecisionGeneratesValidJsonBytes() {
        SecurityDecision blockDecision = SecurityDecision.block(403, "SQL Injection pattern detected");

        assertTrue(blockDecision.isBlocked());
        assertFalse(blockDecision.isAllowed());
        assertEquals(403, blockDecision.getStatusCode());
        assertEquals("SQL Injection pattern detected", blockDecision.getReason());

        byte[] jsonBytes = blockDecision.toJsonBytes();
        String json = new String(jsonBytes, StandardCharsets.UTF_8);

        assertTrue(json.contains("\"error\":\"Forbidden\""));
        assertTrue(json.contains("SQL Injection pattern detected"));
    }
}
