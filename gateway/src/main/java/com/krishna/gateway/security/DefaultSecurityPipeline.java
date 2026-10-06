package com.krishna.gateway.security;

import com.krishna.gateway.model.RequestMetadata;
import com.krishna.gateway.model.SecurityDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * DefaultSecurityPipeline — Phase 6
 *
 * Implements the security analysis step of the gateway pipeline.
 *
 * Responsibilities in Phase 6:
 * 1. Log security metadata event at DEBUG level safely (never logging sensitive credentials)
 * 2. Return an ALLOW decision (baseline placeholder before ML service integration in Phase 13)
 *
 * Extension Point:
 * Future phases will inject MLClient (Phase 13) and RuleBasedDetector (Phase 14B) here.
 */
@Service
public class DefaultSecurityPipeline implements SecurityPipeline {

    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityPipeline.class);

    @Override
    public SecurityDecision evaluate(RequestMetadata metadata) {
        log.debug("Security analysis for {} {} from IP {} (contentLength={}, safeHeaders={})",
                metadata.method(),
                metadata.path(),
                metadata.clientIp(),
                metadata.contentLength(),
                metadata.headers().keySet());

        // In Phase 6, baseline decision is always ALLOW
        return SecurityDecision.allow("Default policy: ALLOW (Phase 6 baseline)");
    }
}
