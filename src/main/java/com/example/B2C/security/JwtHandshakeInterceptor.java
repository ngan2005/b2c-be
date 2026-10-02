package com.example.B2C.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Validates the {@code Authorization} header (or {@code ?token=} query parameter)
 * during the WebSocket handshake. The authenticated user is exposed as the WebSocket
 * session attribute {@code userId}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null) {
            // Allow anonymous handshake; the actual subscription can still be denied later.
            return true;
        }
        try {
            String username = jwtTokenProvider.extractUsername(token);
            if (jwtTokenProvider.isTokenValid(token, username)) {
                attributes.put("userId", jwtTokenProvider.extractUserId(token));
                attributes.put("username", username);
                // Establish a Spring Security context so that @PreAuthorize in
                // controllers receiving STOMP messages can read the principal.
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, java.util.List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException ex) {
            log.debug("WS handshake JWT invalid: {}", ex.getMessage());
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        var headers = request.getHeaders().getFirst("Authorization");
        if (headers != null && headers.startsWith("Bearer ")) {
            return headers.substring(7);
        }
        if (request instanceof ServletServerHttpRequest servlet) {
            String param = servlet.getServletRequest().getParameter("token");
            if (param != null && !param.isBlank()) {
                return param;
            }
        }
        return null;
    }
}
