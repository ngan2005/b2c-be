package com.example.B2C.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserPresenceService {

    // userId -> Set of sessionIds
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    // sessionId -> userId
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();

    public synchronized boolean addSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null) return false;
        sessionToUser.put(sessionId, userId);
        Set<String> sessions = userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        boolean wasOffline = sessions.isEmpty();
        sessions.add(sessionId);
        log.info("[PRESENCE] User {} connected (session={}). Online total: {}", userId, sessionId, userSessions.size());
        return wasOffline;
    }

    public synchronized boolean removeSession(String sessionId) {
        if (sessionId == null) return false;
        Long userId = sessionToUser.remove(sessionId);
        if (userId == null) return false;

        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                log.info("[PRESENCE] User {} now offline. Online total: {}", userId, userSessions.size());
                return true;
            }
        }
        return false;
    }

    public boolean isUserOnline(Long userId) {
        if (userId == null) return false;
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Set<Long> getOnlineUserIds() {
        return Collections.unmodifiableSet(userSessions.keySet());
    }
}
