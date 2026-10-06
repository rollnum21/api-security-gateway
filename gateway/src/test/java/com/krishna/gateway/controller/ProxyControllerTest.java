package com.krishna.gateway.controller;

import com.krishna.gateway.filter.RequestInterceptor;
import com.krishna.gateway.model.SecurityDecision;
import com.krishna.gateway.proxy.ProxyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ProxyController and HealthController verifying:
 * - Local routing for /api/health
 * - GET and POST forwarding through RequestInterceptor and ProxyService
 * - Blocking behavior when RequestInterceptor rejects a request
 */
@WebMvcTest(controllers = {ProxyController.class, HealthController.class})
class ProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private ProxyService proxyService;

    @BeforeEach
    void setUp() {
        // Default behavior: allow requests
        when(requestInterceptor.intercept(any(), any()))
                .thenReturn(SecurityDecision.allow("OK"));
    }

    @Test
    void proxyForwardsProductsEndpointAndReturnsBackendResponse() throws Exception {
        String mockBackendJson = "[\"Laptop\",\"Keyboard\",\"Mouse\"]";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<byte[]> mockBackendResponse = new ResponseEntity<>(
                mockBackendJson.getBytes(StandardCharsets.UTF_8),
                responseHeaders,
                HttpStatus.OK
        );

        when(proxyService.forwardRequest(any(), any())).thenReturn(mockBackendResponse);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("Laptop"))
                .andExpect(jsonPath("$[1]").value("Keyboard"))
                .andExpect(jsonPath("$[2]").value("Mouse"));

        verify(requestInterceptor, times(1)).intercept(any(), any());
        verify(proxyService, times(1)).forwardRequest(any(), any());
    }

    @Test
    void proxyForwardsPostRequestWithRequestBody() throws Exception {
        String requestJson = "{\"name\":\"Monitor\",\"price\":300}";
        String mockBackendResponseJson = "{\"status\":\"CREATED\",\"product\":{\"name\":\"Monitor\",\"price\":300}}";

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<byte[]> mockBackendResponse = new ResponseEntity<>(
                mockBackendResponseJson.getBytes(StandardCharsets.UTF_8),
                responseHeaders,
                HttpStatus.CREATED
        );

        when(proxyService.forwardRequest(any(), any())).thenReturn(mockBackendResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.product.name").value("Monitor"));

        verify(requestInterceptor, times(1)).intercept(any(), any());
        verify(proxyService, times(1)).forwardRequest(any(), any());
    }

    @Test
    void proxyReturns403WhenInterceptorBlocksRequest() throws Exception {
        when(requestInterceptor.intercept(any(), any()))
                .thenReturn(SecurityDecision.block(403, "Detected malicious payload"));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Detected malicious payload"));

        // ProxyService must NOT be called when request is blocked
        verify(proxyService, never()).forwardRequest(any(), any());
    }

    @Test
    void healthEndpointDoesNotHitProxyOrInterceptor() throws Exception {
        // GET /api/health must be handled by HealthController directly
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("API Security Gateway"));

        verify(requestInterceptor, never()).intercept(any(), any());
        verify(proxyService, never()).forwardRequest(any(), any());
    }
}
