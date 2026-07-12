package com.interviewplatform.security.filter;

import com.interviewplatform.common.constants.ApiConstants;
import com.interviewplatform.security.jwt.JwtService;
import com.interviewplatform.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter.
 *
 * <p>Runs once per request. Extracts the Bearer token from the
 * {@code Authorization} header, validates it, loads the user, and
 * sets the authentication in the {@link SecurityContextHolder}.</p>
 *
 * <p><b>Security logging rules:</b></p>
 * <ul>
 *   <li>NEVER logs the token value</li>
 *   <li>NEVER logs the Authorization header</li>
 *   <li>Logs only: method, URI, and validation outcome</li>
 * </ul>
 *
 * <p><b>Sprint 2 — Authentication & User Management</b></p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = MDC.get(ApiConstants.MDC_REQUEST_ID_KEY);
        String token = extractToken(request);

        if (token == null) {
            // No token — pass through; Security will enforce auth if endpoint requires it
            filterChain.doFilter(request, response);
            return;
        }

        // Validate token — never log the token value itself
        if (!jwtService.isTokenValid(token)) {
            log.debug("[{}] JWT validation failed: {} {}", requestId, request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Token is valid — load user and set authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtService.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("[{}] JWT authenticated user: {} {} {}", requestId,
                    email, request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT from the Authorization header.
     *
     * @return the token string without "Bearer " prefix, or {@code null} if absent/malformed
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
