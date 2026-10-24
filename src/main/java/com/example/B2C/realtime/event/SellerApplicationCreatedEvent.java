package com.example.B2C.realtime.event;

import java.time.Instant;

/**
 * Published after a buyer submits a new seller-application (status = PENDING).
 * Admin clients subscribed to /topic/admin/seller-applications receive this.
 */
public record SellerApplicationCreatedEvent(
        Long applicationId,
        String shopName,
        String applicantEmail,
        Instant occurredAt
) {}
