package com.example.B2C.realtime;

import com.example.B2C.modules.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically broadcasts the current admin dashboard KPIs so that admin clients
 * always see a live, up-to-date snapshot without having to poll REST endpoints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardScheduler {

    private final SimpMessagingTemplate template;
    private final StatsService statsService;

    /**
     * Fires every 30 seconds, with a 5-second delay after startup so the app
     * has time to fully initialise before the first broadcast.
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 5_000)
    public void broadcastDashboardSnapshot() {
        var stats = statsService.getAdminStats();
        template.convertAndSend("/topic/admin/dashboard", stats);
        log.info("[REALTIME:admin] dashboard snapshot broadcasted");
    }
}
