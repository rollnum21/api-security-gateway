package com.krishna.gateway.filter;

import com.krishna.gateway.model.RequestMetadata;
import com.krishna.gateway.model.SecurityDecision;
import com.krishna.gateway.security.MetadataExtractor;
import com.krishna.gateway.security.SecurityPipeline;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * RequestInterceptor — Phase 6
 *
 * Intercepts incoming requests destined for downstream endpoints, extracts raw metadata,
 * and passes the request through the security analysis pipeline.
 *
 * Pipeline flow:
 *   Incoming Request -> Metadata Capture -> Security Pipeline Analysis -> Decision (ALLOW/BLOCK)
 */
@Component
public class RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

    private final MetadataExtractor metadataExtractor;
    private final SecurityPipeline securityPipeline;

    public RequestInterceptor(MetadataExtractor metadataExtractor, SecurityPipeline securityPipeline) {
        this.metadataExtractor = metadataExtractor;
        this.securityPipeline = securityPipeline;
    }

    /**
     * Intercepts an incoming HTTP request, captures raw metadata, and evaluates the security policy.
     *
     * @param request the incoming servlet request
     * @param body    the raw request body, if any
     * @return the SecurityDecision (ALLOW or BLOCK)
     */
    public SecurityDecision intercept(HttpServletRequest request, byte[] body) {
        RequestMetadata metadata = metadataExtractor.extract(request, body);
        log.debug("Intercepted request: {}", metadata);
        return securityPipeline.evaluate(metadata);
    }

    public MetadataExtractor getMetadataExtractor() {
        return metadataExtractor;
    }

    public SecurityPipeline getSecurityPipeline() {
        return securityPipeline;
    }
}
