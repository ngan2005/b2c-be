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
public class SellerStatsResponse {
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long pendingOrders;
    private long lowStockProducts;
    private BigDecimal totalRevenue;
    private BigDecimal revenueLast30Days;
}