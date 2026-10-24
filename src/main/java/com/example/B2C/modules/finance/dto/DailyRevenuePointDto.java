package com.example.B2C.modules.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * A single daily revenue data point for the 30-day chart.
 */
@Schema(description = "Daily revenue point")
public record DailyRevenuePointDto(
        @Schema(description = "Date in yyyy-MM-dd format", example = "2026-10-17")
        String date,

        @Schema(description = "GMV for the day", example = "1250000")
        BigDecimal gmv,

        @Schema(description = "Platform commission revenue for the day", example = "87500")
        BigDecimal platformRevenue,

        @Schema(description = "Seller net (GMV - commission) for the day", example = "1162500")
        BigDecimal sellerNet
) {
}
