package com.interviewplatform.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Universal API response envelope for all endpoints.
 *
 * <p><b>Success shape:</b></p>
 * <pre>
 * {
 *   "success":   true,
 *   "message":   "Resource retrieved successfully",
 *   "data":      { ... },
 *   "timestamp": "2026-07-05T13:00:00Z",
 *   "path":      "/api/v1/health",
 *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
 * }
 * </pre>
 *
 * <p><b>Error shape:</b></p>
 * <pre>
 * {
 *   "success":   false,
 *   "message":   "Validation failed",
 *   "error":     { "code": "VALIDATION_ERROR", "message": "...", "details": { ... } },
 *   "timestamp": "2026-07-05T13:00:00Z",
 *   "path":      "/api/v1/interviews",
 *   "requestId": "550e8400-e29b-41d4-a716-446655440000"
 * }
 * </pre>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 *
 * @param <T> the type of the {@code data} payload
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** {@code true} for 2xx responses; {@code false} for all error responses. */
    private final boolean success;

    /** Human-readable message describing the outcome. Always present. */
    private final String message;

    /**
     * Response payload. Present on success; {@code null} on error.
     * Omitted from JSON when {@code null}.
     */
    private final T data;

    /**
     * Error details. Present on failure; {@code null} on success.
     * Omitted from JSON when {@code null}.
     */
    private final ApiError error;

    /** UTC timestamp of when this response was produced. */
    private final Instant timestamp;

    /** Request path — aids frontend debugging and log correlation. */
    private final String path;

    /** Echoes the {@code X-Request-ID} header value for end-to-end tracing. */
    private final String requestId;

    // =========================================================================
    // Static factory methods — success
    // =========================================================================

    /**
     * Build a successful response with data and a custom message.
     *
     * @param message  human-readable success message
     * @param data     the response payload
     * @param path     the request URI
     * @param requestId the X-Request-ID value
     * @param <T>      payload type
     */
    public static <T> ApiResponse<T> success(String message, T data, String path, String requestId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .path(path)
                .requestId(requestId)
                .build();
    }

    /**
     * Build a successful response with data and a default message ("OK").
     */
    public static <T> ApiResponse<T> success(T data, String path, String requestId) {
        return success("OK", data, path, requestId);
    }

    // =========================================================================
    // Static factory methods — error
    // =========================================================================

    /**
     * Build an error response.
     *
     * @param message   human-readable error summary
     * @param error     structured error details
     * @param path      the request URI
     * @param requestId the X-Request-ID value
     * @param <T>       payload type (always {@code Void} for errors)
     */
    public static <T> ApiResponse<T> error(String message, ApiError error, String path, String requestId) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(Instant.now())
                .path(path)
                .requestId(requestId)
                .build();
    }

    /**
     * Convenience builder for simple error responses with just a code and message.
     */
    public static <T> ApiResponse<T> error(String code, String message, String path, String requestId) {
        return error(message, ApiError.of(code, message), path, requestId);
    }
}
