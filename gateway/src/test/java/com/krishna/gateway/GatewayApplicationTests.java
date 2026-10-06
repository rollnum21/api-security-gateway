package com.krishna.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the API Security Gateway.
 *
 * Verifies:
 * 1. Health check routing (/api/health) handled locally by Gateway
 * 2. Proxy routing (/api/products) properly attempts forwarding and returns 502 when backend is down
 */
@SpringBootTest
@AutoConfigureMockMvc
class GatewayApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("API Security Gateway"));
    }

    @Test
    void proxyEndpointReturns502WhenBackendUnavailable() throws Exception {
        // When the downstream backend service is not running, proxy should return 502 Bad Gateway
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Bad Gateway"))
                .andExpect(jsonPath("$.message").exists());
    }
}
