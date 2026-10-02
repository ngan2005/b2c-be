package com.example.B2C.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalSellers;
    private long activeSellers;
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long pendingSellerApplications;
    private BigDecimal totalRevenue;
}