package com.interviewplatform.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.common.response.ApiError;
import com.interviewplatform.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a JSON {@code 401 Unauthorized} response when an unauthenticated
 * request reaches a protected endpoint.
 *
 * <p>Replaces Spring Security's default redirect-based 401 handling with
 * the application's standard {@link ApiResponse} envelope.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.debug("Unauthorized access attempt: {} {}", request.getMethod(), request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.error(
                "UNAUTHORIZED",
                "Authentication required. Please provide a valid Bearer token.",
                request.getRequestURI(),
                (String) request.getAttribute("requestId")
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
