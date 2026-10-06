package com.krishna.gateway.controller;

import com.krishna.gateway.filter.RequestInterceptor;
import com.krishna.gateway.model.SecurityDecision;
import com.krishna.gateway.proxy.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProxyController — Phase 5 & 6
 *
 * Reverse-proxy controller for the API Security Gateway.
 * Intercepts calls destined for the downstream API (under /api/**),
 * passes them through the RequestInterceptor pipeline, and forwards
 * allowed requests to the ProxyService.
 *
 * Flow:
 *   Incoming Request -> Request Interceptor -> Decision -> Forward if allowed
 *
 * Specific gateway endpoints (such as GET /api/health in HealthController)
 * take precedence over this wildcard mapping in Spring MVC pattern resolution.
 */
@RestController
public class ProxyController {

    private final RequestInterceptor interceptor;
    private final ProxyService proxyService;

    public ProxyController(RequestInterceptor interceptor, ProxyService proxyService) {
        this.interceptor = interceptor;
        this.proxyService = proxyService;
    }

    /**
     * Intercepts all methods (GET, POST, PUT, DELETE, etc.) on /api/**, evaluates the security
     * policy, and forwards allowed requests to the backend service.
     *
     * @param request the incoming HTTP request
     * @param body    the raw request body, if any
     * @return the downstream response forwarded back to the client, or 403 Forbidden if blocked
     */
    @RequestMapping(value = "/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        SecurityDecision decision = interceptor.intercept(request, body);
        if (decision.isBlocked()) {
            return ResponseEntity.status(decision.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(decision.toJsonBytes());
        }

        return proxyService.forwardRequest(request, body);
    }
}
