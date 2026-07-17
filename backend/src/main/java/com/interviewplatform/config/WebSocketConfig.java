package com.interviewplatform.config;

import com.interviewplatform.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP/SockJS WebSocket configuration.
 *
 * <p>Endpoint: {@code /ws} (SockJS fallback enabled for browser compatibility)</p>
 * <p>App destination prefix: {@code /app}</p>
 * <p>Topic prefix: {@code /topic}</p>
 *
 * <p>JWT authentication is validated on {@code CONNECT} frames via a
 * {@link ChannelInterceptor} — the token is read from the STOMP
 * {@code Authorization} header.</p>
 *
 * <p><b>Module:</b> Module 3 — Interview Orchestrator</p>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // =========================================================================
    // Message broker
    // =========================================================================

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory broker for /topic destinations
        registry.enableSimpleBroker("/topic");
        // All @MessageMapping methods are prefixed with /app
        registry.setApplicationDestinationPrefixes("/app");
    }

    // =========================================================================
    // STOMP endpoint
    // =========================================================================

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // =========================================================================
    // JWT Channel Interceptor
    // =========================================================================

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            String username = jwtService.extractEmail(token);
                            if (username != null && jwtService.isTokenValid(token)) {
                                var userDetails = userDetailsService.loadUserByUsername(username);
                                var authToken = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                accessor.setUser(authToken);
                                log.debug("WebSocket CONNECT authenticated: user={}", username);
                            }
                        } catch (Exception ex) {
                            log.warn("WebSocket JWT validation failed: {}", ex.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}
