package com.example.B2C.realtime.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published after a buyer creates a new order (status = PENDING).
 * Admin clients subscribed to /topic/admin/orders receive this.
 */
public record OrderCreatedEvent(
        Long orderId,
        String orderCode,
        Long buyerId,
        Long sellerId,
        BigDecimal totalAmount,
        Instant occurredAt
) {}
