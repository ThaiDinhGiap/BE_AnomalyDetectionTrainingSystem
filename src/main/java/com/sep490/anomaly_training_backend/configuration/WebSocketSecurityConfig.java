package com.sep490.anomaly_training_backend.configuration;

import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * Xác thực JWT khi client kết nối STOMP.
 *
 * <p>Flow:</p>
 * <ol>
 *   <li>Client gửi CONNECT frame với header {@code Authorization: Bearer {token}}</li>
 *   <li>Interceptor này extract token, validate, set Principal</li>
 *   <li>Principal name = userId (String) — dùng để route /user/{userId}/queue/...</li>
 * </ol>
 *
 * <p>Nếu token invalid → CONNECT bị reject, không crash server.</p>
 */
@Configuration
@ConditionalOnProperty(name = "app.websocket.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) return message;

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            String username = jwtService.extractUsername(token);

                            if (username != null) {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                                if (jwtService.isTokenValid(token, userDetails)) {
                                    User user = (User) userDetails;

                                    Principal principal = () -> user.getId().toString();
                                    accessor.setUser(principal);

                                    log.debug("[WebSocket] CONNECT authenticated userId={}", user.getId());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("[WebSocket] CONNECT rejected — invalid token: {}", e.getMessage());
                            return null;
                        }
                    } else {
                        log.warn("[WebSocket] CONNECT without Authorization header — rejected");
                        return null;
                    }
                }

                return message;
            }
        });
    }
}