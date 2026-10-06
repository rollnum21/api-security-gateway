package com.krishna.gateway.security;

import com.krishna.gateway.model.RequestMetadata;
import com.krishna.gateway.model.SecurityDecision;

/**
 * SecurityPipeline — Phase 6
 *
 * Defines the contract for evaluating an intercepted HTTP request.
 * In Phase 6, this component acts as a placeholder pipeline that returns ALLOW.
 * In Phase 13/14, this will orchestrate ML anomaly scoring and rule-based inspection.
 */
public interface SecurityPipeline {

    /**
     * Evaluates raw request metadata and returns a security decision (ALLOW or BLOCK).
     *
     * @param metadata the raw request metadata
     * @return the security decision
     */
    SecurityDecision evaluate(RequestMetadata metadata);
}
