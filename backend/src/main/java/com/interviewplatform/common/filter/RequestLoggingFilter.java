package com.interviewplatform.common.filter;

import com.interviewplatform.common.constants.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that runs once per request to:
 * <ol>
 *   <li>Assign or reuse a {@code X-Request-ID} for end-to-end tracing.</li>
 *   <li>Populate MDC so all log statements in the request thread include the ID.</li>
 *   <li>Echo the {@code X-Request-ID} in the response header.</li>
 *   <li>Log a single structured line per request after the response is committed.</li>
 * </ol>
 *
 * <p><b>Log format:</b></p>
 * <pre>
 * [uuid] GET /api/v1/health → 200 (12ms) | UA: PostmanRuntime/7.x | IP: 127.0.0.1
 * </pre>
 *
 * <p><b>Sprint 1 — Backend Foundation</b></p>
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // ---- 1. Resolve Request ID ----
        String requestId = resolveRequestId(request);

        // ---- 2. Populate MDC ----
        MDC.put(ApiConstants.MDC_REQUEST_ID, requestId);

        // ---- 3. Echo header in response ----
        response.setHeader(ApiConstants.REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // ---- 4. Log after response committed ----
            long duration     = System.currentTimeMillis() - startTime;
            int  status       = response.getStatus();
            String method     = request.getMethod();
            String uri        = request.getRequestURI();
            String userAgent  = resolveUserAgent(request);
            String remoteAddr = resolveRemoteAddress(request);

            log.info("[{}] {} {} → {} ({}ms) | UA: {} | IP: {}",
                    requestId, method, uri, status, duration, userAgent, remoteAddr);

            // ---- 5. Clear MDC to prevent thread-local leakage ----
            MDC.remove(ApiConstants.MDC_REQUEST_ID);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Returns the incoming {@code X-Request-ID} header value if present and non-blank;
     * otherwise generates a new random UUID.
     */
    private String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(ApiConstants.REQUEST_ID_HEADER);
        return StringUtils.hasText(incoming) ? incoming : UUID.randomUUID().toString();
    }

    /**
     * Returns the {@code User-Agent} header, or {@code "-"} if absent.
     */
    private String resolveUserAgent(HttpServletRequest request) {
        String ua = request.getHeader(ApiConstants.USER_AGENT_HEADER);
        return StringUtils.hasText(ua) ? ua : "-";
    }

    /**
     * Resolves the real client IP address, respecting the {@code X-Forwarded-For}
     * header set by reverse proxies (Nginx).
     */
    private String resolveRemoteAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            // X-Forwarded-For may contain a comma-separated list; the first is the original client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
