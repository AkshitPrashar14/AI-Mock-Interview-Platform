package com.interviewplatform.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Canonical error detail object embedded inside {@link ApiResponse} on failure.
 *
 * <pre>
 * {
 *   "code":    "VALIDATION_ERROR",
 *   "message": "Request body failed validation",
 *   "details": { "fieldName": "must not be blank" }
 * }
 * </pre>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /**
     * Machine-readable error code — uppercase, underscore-separated.
     * Consumers should switch on this value, not on the HTTP status code alone.
     */
    private final String code;

    /** Human-readable error message suitable for logging and debugging. */
    private final String message;

    /**
     * Optional structured details (e.g. per-field validation errors).
     * Omitted from the response when {@code null}.
     */
    private final Object details;

    // ---- Static Factories ----

    public static ApiError of(String code, String message) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static ApiError of(String code, String message, Object details) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}
