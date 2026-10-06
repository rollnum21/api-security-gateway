package com.krishna.gateway.filter;

import com.krishna.gateway.model.RequestMetadata;
import com.krishna.gateway.model.SecurityDecision;
import com.krishna.gateway.security.MetadataExtractor;
import com.krishna.gateway.security.SecurityPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestInterceptor.
 */
class RequestInterceptorTest {

    private MetadataExtractor metadataExtractor;
    private SecurityPipeline securityPipeline;
    private RequestInterceptor interceptor;

    @BeforeEach
    void setUp() {
        metadataExtractor = mock(MetadataExtractor.class);
        securityPipeline = mock(SecurityPipeline.class);
        interceptor = new RequestInterceptor(metadataExtractor, securityPipeline);
    }

    @Test
    void interceptDelegatesToExtractorAndPipeline() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        RequestMetadata metadata = new RequestMetadata(
                java.time.Instant.now(), "GET", "/api/products", null, null, 0, "127.0.0.1", null, null
        );

        when(metadataExtractor.extract(request, null)).thenReturn(metadata);
        when(securityPipeline.evaluate(metadata)).thenReturn(SecurityDecision.allow("OK"));

        SecurityDecision decision = interceptor.intercept(request, null);

        assertTrue(decision.isAllowed());
        verify(metadataExtractor, times(1)).extract(request, null);
        verify(securityPipeline, times(1)).evaluate(metadata);
    }

    @Test
    void interceptReturnsBlockWhenPipelineRejects() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/products");
        byte[] body = "{\"attack\":\"' OR 1=1\"}".getBytes(StandardCharsets.UTF_8);
        RequestMetadata metadata = new RequestMetadata(
                java.time.Instant.now(), "POST", "/api/products", null, "application/json", body.length, "127.0.0.1", null, body
        );

        when(metadataExtractor.extract(request, body)).thenReturn(metadata);
        when(securityPipeline.evaluate(metadata)).thenReturn(SecurityDecision.block(403, "Anomaly detected"));

        SecurityDecision decision = interceptor.intercept(request, body);

        assertTrue(decision.isBlocked());
        assertEquals(403, decision.getStatusCode());
        assertEquals("Anomaly detected", decision.getReason());
    }
}
