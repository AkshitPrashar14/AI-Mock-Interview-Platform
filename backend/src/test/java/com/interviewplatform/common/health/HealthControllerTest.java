package com.interviewplatform.common.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.common.constants.ApiConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for {@link HealthController}.
 *
 * <p>Uses {@code @SpringBootTest} with the full application context to verify
 * that the health endpoint is wired correctly end-to-end.</p>
 *
 * <p><b>Note:</b> This test requires a running PostgreSQL instance (or an
 * embedded/test-containers setup). For Sprint 1, the test is run against the
 * dev profile with the Docker Compose PostgreSQL container running.</p>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("HealthController Integration Tests")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================================
    // Tests
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/health returns HTTP 200")
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/health returns success=true")
    void healthEndpoint_returnsSuccessTrue() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(body.get("success").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/health returns status=UP")
    void healthEndpoint_returnsStatusUp() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = body.get("data");

        assertThat(data).isNotNull();
        assertThat(data.get("status").asText()).isEqualTo("UP");
    }

    @Test
    @DisplayName("GET /api/v1/health response contains all required fields")
    void healthEndpoint_containsAllRequiredFields() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = body.get("data");

        assertThat(data.has("status")).isTrue();
        assertThat(data.has("service")).isTrue();
        assertThat(data.has("version")).isTrue();
        assertThat(data.has("environment")).isTrue();
        assertThat(data.has("timestamp")).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/health returns X-Request-ID response header")
    void healthEndpoint_returnsRequestIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(ApiConstants.REQUEST_ID_HEADER));
    }

    @Test
    @DisplayName("GET /api/v1/health echoes incoming X-Request-ID header")
    void healthEndpoint_echoesIncomingRequestId() throws Exception {
        String customRequestId = "test-request-id-12345";

        MvcResult result = mockMvc.perform(
                        get("/api/v1/health")
                                .header(ApiConstants.REQUEST_ID_HEADER, customRequestId))
                .andExpect(status().isOk())
                .andReturn();

        String echoedId = result.getResponse().getHeader(ApiConstants.REQUEST_ID_HEADER);
        assertThat(echoedId).isEqualTo(customRequestId);
    }

    @Test
    @DisplayName("GET /api/v1/health returns service name from constants")
    void healthEndpoint_returnsCorrectServiceName() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("data").get("service").asText())
                .isEqualTo(ApiConstants.SERVICE_NAME);
    }
}
