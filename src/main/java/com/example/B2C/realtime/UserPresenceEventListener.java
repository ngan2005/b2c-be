package com.example.B2C.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPresenceEventListener {

    private final UserPresenceService userPresenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.example.B2C.modules.user.repository.UserRepository userRepository;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attrs = accessor.getSessionAttributes();

        Object rawId = (attrs != null) ? attrs.get("userId") : null;
        String sessionId = accessor.getSessionId();
        Long userId = parseUserId(rawId);

        // Fallback: Check accessor user or event user if userId not yet found in attrs
        if (userId == null) {
            java.security.Principal principal = accessor.getUser() != null ? accessor.getUser() : event.getUser();
            if (principal != null && principal.getName() != null) {
                String username = principal.getName();
                userId = userRepository.findByEmail(username)
                        .map(com.example.B2C.modules.user.entity.User::getId)
                        .orElse(null);
                if (userId != null && attrs != null) {
                    attrs.put("userId", userId);
                }
            }
        }

        if (userId != null && sessionId != null) {
            boolean wasOffline = userPresenceService.addSession(userId, sessionId);
            if (wasOffline) {
                broadcastPresence(userId, true);
            }
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            Long offlineUserId = userPresenceService.removeSessionAndCheckOffline(sessionId);
            if (offlineUserId != null) {
                broadcastPresence(offlineUserId, false);
            }
        }
    }

    public record PresenceMessage(Long userId, boolean isOnline, String timestamp) {}

    private void broadcastPresence(Long userId, boolean isOnline) {
        try {
            PresenceMessage message = new PresenceMessage(userId, isOnline, Instant.now().toString());
            messagingTemplate.convertAndSend("/topic/admin/presence", (Object) message);
            log.info("[PRESENCE:ws] Broadcast user {} status online={}", userId, isOnline);
        } catch (Exception ex) {
            log.warn("Failed to broadcast user presence update: {}", ex.getMessage());
        }
    }

    private Long parseUserId(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
