package com.interviewplatform.common.health;

import com.interviewplatform.common.constants.ApiConstants;
import com.interviewplatform.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Production-grade health check endpoint.
 *
 * <p>Intended for use by load balancers, Docker health checks, and operations
 * dashboards to verify the application is running and accepting traffic.</p>
 *
 * <p>This endpoint is always publicly accessible — no authentication required.</p>
 *
 * <p><b>Example response:</b></p>
 * <pre>
 * GET /api/v1/health
 * {
 *   "success":   true,
 *   "message":   "Service is healthy",
 *   "data": {
 *     "status":      "UP",
 *     "service":     "ai-mock-interview-platform",
 *     "version":     "1.0.0",
 *     "environment": "dev",
 *     "timestamp":   "2026-07-05T13:00:00Z"
 *   },
 *   "timestamp":  "2026-07-05T13:00:00Z",
 *   "path":       "/api/v1/health",
 *   "requestId":  "550e8400-e29b-41d4-a716-446655440000"
 * }
 * </pre>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Service health and liveness check")
public class HealthController {

    private final Clock clock;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // =========================================================================
    // Endpoints
    // =========================================================================

    /**
     * Returns the current health status of the service.
     *
     * <p>HTTP 200 indicates the service is UP and ready to serve requests.</p>
     */
    @GetMapping
    @Operation(
            summary = "Health check",
            description = "Returns the current health status of the service. " +
                    "Always accessible without authentication."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "Service is healthy and accepting requests"
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> health(HttpServletRequest request) {
        String requestId = MDC.get(ApiConstants.MDC_REQUEST_ID);

        Map<String, Object> healthData = new LinkedHashMap<>();
        healthData.put("status",      "UP");
        healthData.put("service",     ApiConstants.SERVICE_NAME);
        healthData.put("version",     ApiConstants.APP_VERSION);
        healthData.put("environment", activeProfile);
        healthData.put("timestamp",   Instant.now(clock).toString());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Service is healthy",
                        healthData,
                        request.getRequestURI(),
                        requestId
                )
        );
    }
}
