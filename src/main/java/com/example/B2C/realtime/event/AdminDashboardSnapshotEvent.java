package com.example.B2C.realtime.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Broadcast periodically (every 30 s) by {@link com.example.B2C.realtime.AdminDashboardScheduler}.
 * Admin clients subscribed to /topic/admin/dashboard receive this snapshot.
 */
public record AdminDashboardSnapshotEvent(
        long totalUsers,
        long activeUsers,
        long totalSellers,
        long activeSellers,
        long totalProducts,
        long totalOrders,
        long pendingSellerApplications,
        BigDecimal totalRevenue,
        Instant generatedAt
) {}
