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
public class TopSellerPoint {
    private Long sellerId;
    private String shopName;
    private BigDecimal totalRevenue;
    private long orderCount;
}
