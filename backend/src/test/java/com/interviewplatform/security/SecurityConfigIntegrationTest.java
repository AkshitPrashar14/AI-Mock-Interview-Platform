package com.interviewplatform.security;

import com.interviewplatform.common.health.HealthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Spring Security configuration.
 *
 * <p>Verifies that public endpoints are accessible without authentication
 * and protected endpoints return 401 when no token is provided.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("Security Configuration Integration Tests")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Public endpoints — should return 200 without any token
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/health is accessible without authentication")
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /swagger-ui.html is accessible without authentication")
    void swaggerUi_isPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // Swagger redirects to /swagger-ui/index.html
    }

    @Test
    @DisplayName("GET /v3/api-docs is accessible without authentication")
    void apiDocs_isPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // Protected endpoints — should return 401 without a token
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/auth/me returns 401 without token")
    void me_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/profile returns 401 without token")
    void updateProfile_returns401WithoutToken() throws Exception {
        mockMvc.perform(patch("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"firstName\": \"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request with malformed Bearer token returns 401")
    void malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request with 'Bearer ' only (no token) returns 401")
    void bearerWithNoToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }
}
