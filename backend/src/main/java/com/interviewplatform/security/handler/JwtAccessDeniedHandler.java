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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns a JSON {@code 403 Forbidden} response when an authenticated user
 * attempts to access a resource they are not authorized to access.
 *
 * <p>Replaces Spring Security's default 403 handling with the application's
 * standard {@link ApiResponse} envelope.</p>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.debug("Access denied: {} {}", request.getMethod(), request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.error(
                "FORBIDDEN",
                "You do not have permission to access this resource.",
                request.getRequestURI(),
                (String) request.getAttribute("requestId")
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
