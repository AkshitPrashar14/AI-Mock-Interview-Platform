package com.interviewplatform.common.exception;

import com.interviewplatform.auth.exception.InvalidCredentialsException;
import com.interviewplatform.auth.exception.RefreshTokenExpiredException;
import com.interviewplatform.auth.exception.RefreshTokenNotFoundException;
import com.interviewplatform.common.constants.ApiConstants;
import com.interviewplatform.common.response.ApiError;
import com.interviewplatform.common.response.ApiResponse;
import com.interviewplatform.interview.exception.InterviewAlreadyStartedException;
import com.interviewplatform.interview.exception.InterviewNotFoundException;
import com.interviewplatform.interview.exception.InvalidStateTransitionException;
import com.interviewplatform.report.service.ReportNotFoundException;
import com.interviewplatform.user.exception.UnauthorizedException;
import com.interviewplatform.user.exception.UserAlreadyExistsException;
import com.interviewplatform.user.exception.UserNotFoundException;
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
    // Sprint 2 — Auth & User exceptions
    // =========================================================================

    /** 409 Conflict — duplicate email registration. */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {
        log.warn("[{}] User already exists: {}", requestId(), ex.getMessage());
        ApiError error = ApiError.of("USER_ALREADY_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 404 Not Found — user lookup by id or email failed. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {
        log.warn("[{}] User not found: {}", requestId(), ex.getMessage());
        ApiError error = ApiError.of("USER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 401 Unauthorized — invalid email/password combination. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        log.warn("[{}] Invalid credentials: {} {}", requestId(), request.getMethod(), request.getRequestURI());
        ApiError error = ApiError.of("INVALID_CREDENTIALS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 401 Unauthorized — refresh token expired. */
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenExpired(
            RefreshTokenExpiredException ex,
            HttpServletRequest request) {
        log.warn("[{}] Refresh token expired: {} {}", requestId(), request.getMethod(), request.getRequestURI());
        ApiError error = ApiError.of("REFRESH_TOKEN_EXPIRED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 401 Unauthorized — refresh token not found or revoked. */
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenNotFound(
            RefreshTokenNotFoundException ex,
            HttpServletRequest request) {
        log.warn("[{}] Refresh token not found: {} {}", requestId(), request.getMethod(), request.getRequestURI());
        ApiError error = ApiError.of("REFRESH_TOKEN_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 401 Unauthorized — generic unauthorized access. */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {
        log.warn("[{}] Unauthorized: {} {}", requestId(), request.getMethod(), request.getRequestURI());
        ApiError error = ApiError.of("UNAUTHORIZED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    // =========================================================================
    // Module 2 — Interview Session Management exceptions
    // =========================================================================

    /** 404 Not Found — interview does not exist or does not belong to the candidate. */
    @ExceptionHandler(InterviewNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewNotFound(
            InterviewNotFoundException ex,
            HttpServletRequest request) {
        log.warn("[{}] Interview not found: {}", requestId(), ex.getMessage());
        ApiError error = ApiError.of("INTERVIEW_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 409 Conflict — state transition not permitted by the state machine. */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStateTransition(
            InvalidStateTransitionException ex,
            HttpServletRequest request) {
        log.warn("[{}] Invalid state transition: {}", requestId(), ex.getMessage());
        ApiError error = ApiError.of("INVALID_STATE_TRANSITION", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 409 Conflict — attempted to start an already-started interview. */
    @ExceptionHandler(InterviewAlreadyStartedException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewAlreadyStarted(
            InterviewAlreadyStartedException ex,
            HttpServletRequest request) {
        log.warn("[{}] Interview already started: {}", requestId(), ex.getMessage());
        ApiError error = ApiError.of("INTERVIEW_ALREADY_STARTED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
    }

    /** 404 Not Found — report does not exist yet (may still be generating). */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleReportNotFound(
            ReportNotFoundException ex,
            HttpServletRequest request) {
        log.warn("[{}] Report not found: {}", requestId(), ex.getMessage());
        ApiError error = ApiError.of("REPORT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), error, request.getRequestURI(), requestId()));
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
        String id = MDC.get(ApiConstants.MDC_REQUEST_ID_KEY);
        return (id != null) ? id : "-";
    }
}
