package com.krishna.gateway.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ProxyService — Phase 5
 *
 * Handles reverse-proxying HTTP requests from the gateway (port 8080)
 * to the target backend service (configured via backend.url, default port 3000).
 *
 * Responsibilities:
 * 1. Target URL construction preserving path and query parameters
 * 2. Header forwarding (filtering hop-by-hop headers, adding X-Forwarded-* headers)
 * 3. Body forwarding for mutating methods (POST, PUT, PATCH)
 * 4. Error isolation (returns 502 Bad Gateway if downstream backend is unreachable)
 * 5. Returning the downstream status code, headers, and body as-is
 */
@Service
public class ProxyService {

    private static final Logger log = LoggerFactory.getLogger(ProxyService.class);

    private final String backendUrl;
    private final RestTemplate restTemplate;

    /**
     * Standard HTTP hop-by-hop headers that must not be forwarded by a proxy (RFC 2616 / RFC 7230).
     */
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailers",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length"
    );

    public ProxyService(@Value("${backend.url:http://localhost:3000}") String backendUrl,
                        RestTemplateBuilder restTemplateBuilder) {
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse response) {
                        // Do not throw exceptions on 4xx or 5xx; allow backend responses to pass through
                        return false;
                    }
                })
                .build();
    }

    /**
     * Forwards an incoming HTTP request to the target backend API.
     *
     * @param request the incoming servlet request
     * @param body    the raw request body, if any
     * @return the HTTP response to return to the caller
     */
    public ResponseEntity<byte[]> forwardRequest(HttpServletRequest request, byte[] body) {
        URI targetUri;
        try {
            targetUri = buildTargetUri(request);
        } catch (Exception e) {
            log.error("Failed to construct target backend URI for path {}: {}", request.getRequestURI(), e.getMessage());
            byte[] errorJson = "{\"error\":\"Bad Request\",\"message\":\"Invalid request URI\"}".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson);
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase(Locale.ROOT));
        HttpHeaders forwardHeaders = buildForwardHeaders(request);

        log.debug("Proxying {} {} -> {}", method, request.getRequestURI(), targetUri);

        RequestEntity<byte[]> requestEntity = new RequestEntity<>(body, forwardHeaders, method, targetUri);

        try {
            ResponseEntity<byte[]> backendResponse = restTemplate.exchange(requestEntity, byte[].class);
            return buildClientResponse(backendResponse);
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to backend at {}: {}", targetUri, e.getMessage());
            byte[] errorJson = String.format("{\"error\":\"Bad Gateway\",\"message\":\"Target backend service is unreachable at %s\"}", backendUrl)
                    .getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson);
        } catch (Exception e) {
            log.error("Unexpected error proxying to backend: {}", e.getMessage(), e);
            byte[] errorJson = "{\"error\":\"Internal Server Error\",\"message\":\"Gateway proxy error\"}".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson);
        }
    }

    /**
     * Constructs the target backend URI while preserving path and query string.
     * Uses the multi-argument URI constructor to safely handle special characters in query strings.
     */
    public URI buildTargetUri(HttpServletRequest request) throws Exception {
        URI base = URI.create(backendUrl);
        String path = request.getRequestURI();
        String query = request.getQueryString();

        return new URI(
                base.getScheme(),
                base.getUserInfo(),
                base.getHost(),
                base.getPort(),
                path,
                query,
                null
        );
    }

    /**
     * Filters incoming headers and appends standard proxy headers (X-Forwarded-*).
     */
    private HttpHeaders buildForwardHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                Enumeration<String> values = request.getHeaders(headerName);
                while (values.hasMoreElements()) {
                    headers.add(headerName, values.nextElement());
                }
            }
        }

        // Add standard X-Forwarded headers
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null) {
            headers.add("X-Forwarded-For", remoteAddr);
        }
        headers.add("X-Forwarded-Proto", request.getScheme());
        String host = request.getHeader("Host");
        headers.add("X-Forwarded-Host", host != null ? host : request.getServerName());

        return headers;
    }

    /**
     * Copies the downstream backend response, stripping any hop-by-hop headers.
     */
    private ResponseEntity<byte[]> buildClientResponse(ResponseEntity<byte[]> backendResponse) {
        HttpHeaders clientHeaders = new HttpHeaders();

        for (Map.Entry<String, List<String>> entry : backendResponse.getHeaders().entrySet()) {
            String headerName = entry.getKey();
            if (HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase(Locale.ROOT))) {
                continue;
            }
            clientHeaders.put(headerName, entry.getValue());
        }

        return new ResponseEntity<>(
                backendResponse.getBody(),
                clientHeaders,
                backendResponse.getStatusCode()
        );
    }

    public String getBackendUrl() {
        return backendUrl;
    }
}
