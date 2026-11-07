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

    @Schema(description = "Total pending in escrow (held during return window)")
    private BigDecimal totalPending;

    @Schema(description = "Total ready / available balance to withdraw")
    private BigDecimal totalReady;

    @Schema(description = "Total amount already paid out to this seller")
    private BigDecimal totalPaid;

    @Schema(description = "Lifetime total net earnings (pending + ready + paid)")
    private BigDecimal lifetimeEarned;

    @Schema(description = "Seller shop name")
    private String shopName;

    @Schema(description = "Recent payout records (most recent first)")
    private List<SellerPayoutDto> recentPayouts;
}
