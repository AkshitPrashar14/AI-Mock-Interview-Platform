package com.interviewplatform.common.exception;

import com.interviewplatform.common.constants.ApiConstants;
import com.interviewplatform.common.response.ApiError;
import com.interviewplatform.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler — intercepts all unhandled exceptions thrown by controllers
 * and converts them into a consistent {@link ApiResponse} error envelope.
 *
 * <p>All handled exceptions are logged at an appropriate level. 4xx errors are
 * logged at WARN; 5xx errors at ERROR.</p>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 400 — Validation errors
    // =========================================================================

    /**
     * Handles {@code @Valid} / {@code @Validated} failures on request body DTOs.
     * Returns per-field error details in the {@code error.details} map.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("[{}] Validation failed on {} {}: {}",
                requestId(), request.getMethod(), request.getRequestURI(), fieldErrors);

        ApiError error = ApiError.of("VALIDATION_ERROR", "Request body validation failed", fieldErrors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", error, request.getRequestURI(), requestId()));
    }

    /**
     * Handles constraint violations thrown from service-layer method parameter validation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        Map<String, String> violations = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            violations.put(cv.getPropertyPath().toString(), cv.getMessage());
        }

        log.warn("[{}] Constraint violation on {} {}: {}",
                requestId(), request.getMethod(), request.getRequestURI(), violations);

        ApiError error = ApiError.of("VALIDATION_ERROR", "Constraint violation", violations);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", error, request.getRequestURI(), requestId()));
    }

    // =========================================================================
    // 404 — Resource not found
    // =========================================================================

    /**
     * Handles requests to non-existent static resources or unregistered paths.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.warn("[{}] Resource not found: {} {}", requestId(), request.getMethod(), request.getRequestURI());

        ApiError error = ApiError.of("RESOURCE_NOT_FOUND", "The requested resource was not found");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found", error, request.getRequestURI(), requestId()));
    }

    // =========================================================================
    // 405 — Method not allowed
    // =========================================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("[{}] Method not allowed: {} {}", requestId(), request.getMethod(), request.getRequestURI());

        ApiError error = ApiError.of("METHOD_NOT_ALLOWED",
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint");
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("Method not allowed", error, request.getRequestURI(), requestId()));
    }

    // =========================================================================
    // 500 — Catch-all
    // =========================================================================

    /**
     * Last-resort handler. Logs the full stack trace and returns a generic error
     * response — never exposing internal details to the caller.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("[{}] Unhandled exception on {} {}: {}",
                requestId(), request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        ApiError error = ApiError.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", error, request.getRequestURI(), requestId()));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Reads requestId from MDC (set by RequestLoggingFilter). Falls back to "-". */
    private String requestId() {
        String id = MDC.get(ApiConstants.MDC_REQUEST_ID);
        return (id != null) ? id : "-";
    }
}
