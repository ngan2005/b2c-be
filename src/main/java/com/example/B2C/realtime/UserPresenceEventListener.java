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

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) return;

        Object rawId = attrs.get("userId");
        String sessionId = accessor.getSessionId();
        Long userId = parseUserId(rawId);

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
        Map<String, Object> attrs = accessor.getSessionAttributes();
        Long userId = (attrs != null) ? parseUserId(attrs.get("userId")) : null;

        if (sessionId != null) {
            boolean becameOffline = userPresenceService.removeSession(sessionId);
            if (becameOffline && userId != null) {
                broadcastPresence(userId, false);
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
