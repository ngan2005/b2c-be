package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.SellerPayout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Seller statement: summary totals + recent payout history")
public class SellerStatementDto {

    @Schema(description = "Total pending (unpaid) net amount for this seller")
    private BigDecimal totalPending;

    @Schema(description = "Total amount already paid out to this seller")
    private BigDecimal totalPaid;

    @Schema(description = "Lifetime total (gross earned, sum of all completed orders' net amounts)")
    private BigDecimal lifetimeEarned;

    @Schema(description = "Recent payout records (most recent first)")
    private List<SellerPayoutDto> recentPayouts;
}
