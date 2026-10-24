package com.example.B2C.modules.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Aggregated finance KPIs for the admin dashboard.
 *
 * @param gmv                total order GMV (sum of total_amount for COMPLETED orders)
 * @param platformRevenue    sum of commission_amount for COMPLETED orders
 * @param sellerNet          gmv - platformRevenue
 * @param pendingPayout      sum of net_amount for PENDING payouts
 * @param paidPayout         sum of net_amount for PAID payouts
 * @param completedOrders    count of COMPLETED orders
 * @param pendingPayoutsCount count of payouts with status PENDING
 * @param topCategories      top 5 categories by GMV
 */
@Schema(description = "Finance overview KPIs for the admin dashboard")
public record FinanceOverviewDto(
        @Schema(description = "Total GMV (sum of total_amount for COMPLETED orders)", example = "125000000")
        BigDecimal gmv,

        @Schema(description = "Platform revenue (sum of commission_amount for COMPLETED orders)", example = "8750000")
        BigDecimal platformRevenue,

        @Schema(description = "Seller net = GMV - platformRevenue", example = "116250000")
        BigDecimal sellerNet,

        @Schema(description = "Total net amount of PENDING payouts", example = "15000000")
        BigDecimal pendingPayout,

        @Schema(description = "Total net amount of PAID payouts", example = "95000000")
        BigDecimal paidPayout,

        @Schema(description = "Count of COMPLETED orders", example = "342")
        long completedOrders,

        @Schema(description = "Count of PENDING payouts", example = "27")
        long pendingPayoutsCount,

        @Schema(description = "Top 5 categories by GMV", implementation = TopCategoryDto.class)
        java.util.List<TopCategoryDto> topCategories
) {
}
