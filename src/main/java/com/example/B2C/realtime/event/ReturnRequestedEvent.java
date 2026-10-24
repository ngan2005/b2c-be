package com.example.B2C.realtime.event;

import java.time.Instant;

/**
 * Published after a buyer requests a return for a delivered order
 * (order transitions to RETURN_REQUESTED).
 * Admin clients subscribed to /topic/admin/returns receive this.
 */
public record ReturnRequestedEvent(
        Long orderId,
        String orderCode,
        Long buyerId,
        Long sellerId,
        String reason,
        Instant occurredAt
) {}
