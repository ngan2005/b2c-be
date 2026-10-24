package com.example.B2C.realtime.event;

import java.time.Instant;

/**
 * Published after a buyer opens a new dispute on an order.
 * Admin clients subscribed to /topic/admin/disputes receive this.
 */
public record DisputeOpenedEvent(
        Long orderId,
        String orderCode,
        Long buyerId,
        String disputeType,
        String description,
        Instant occurredAt
) {}
