package com.example.B2C.config;

import com.example.B2C.security.JwtHandshakeInterceptor;
import com.example.B2C.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket configuration with heartbeat support and JWT channel
 * authentication.
 * Clients can connect via pure WebSocket or SockJS at {@code /ws}.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.example.B2C.modules.user.repository.UserRepository userRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("ws-heartbeat-");
        taskScheduler.initialize();

        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] { 10000, 10000 })
                .setTaskScheduler(taskScheduler);

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Pure WebSocket endpoint
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*");

        // 2. SockJS fallback endpoint
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    String token = null;
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                    if (token == null && accessor.getSessionAttributes() != null) {
                        token = (String) accessor.getSessionAttributes().get("token");
                    }
                    if (token != null) {
                        try {
                            String username = jwtTokenProvider.extractUsername(token);
                            if (username != null && jwtTokenProvider.isTokenValid(token, username)) {
                                Long userId = jwtTokenProvider.extractUserId(token);
                                if (userId == null) {
                                    userId = userRepository.findByEmail(username)
                                            .map(com.example.B2C.modules.user.entity.User::getId)
                                            .orElse(null);
                                }
                                java.util.Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                                if (sessionAttrs != null) {
                                    if (userId != null) {
                                        sessionAttrs.put("userId", userId);
                                    }
                                    sessionAttrs.put("username", username);
                                }
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        username, null, java.util.List.of());
                                accessor.setUser(auth);
                            }
                        } catch (Exception ignored) {
                        }
                    } else if (accessor.getSessionAttributes() != null) {
                        Object usernameObj = accessor.getSessionAttributes().get("username");
                        if (usernameObj != null) {
                            String username = usernameObj.toString();
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    username, null, java.util.List.of());
                            accessor.setUser(auth);
                        }
                    }
                }
                return message;
            }
        });
    }
}
