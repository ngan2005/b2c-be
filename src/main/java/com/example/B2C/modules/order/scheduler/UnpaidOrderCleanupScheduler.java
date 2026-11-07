package com.example.B2C.modules.order.scheduler;

import com.example.B2C.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically scans for unpaid orders that have exceeded the TTL window (default 15 minutes)
 * and automatically cancels them, releasing their reserved inventory back to available stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnpaidOrderCleanupScheduler {

    private final OrderService orderService;

    /**
     * Runs every 60 seconds (1 minute), with a 15-second initial delay after startup.
     */
    @Scheduled(fixedDelayString = "${app.order.unpaid-cleanup-interval-ms:60000}", initialDelay = 15_000)
    public void cleanupExpiredUnpaidOrders() {
        try {
            int cancelledCount = orderService.cancelExpiredUnpaidOrders();
            if (cancelledCount > 0) {
                log.info("[CLEANUP:orders] Auto-cancelled {} expired unpaid orders and restored reserved inventory",
                        cancelledCount);
            }
        } catch (Exception ex) {
            log.error("[CLEANUP:orders] Error during unpaid order cleanup: {}", ex.getMessage(), ex);
        }
    }
}
