package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.PayoutStatus;
import com.example.B2C.modules.finance.entity.SellerPayout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Seller payout record DTO")
public class SellerPayoutDto {

    @Schema(description = "Payout ID")
    private Long id;

    @Schema(description = "Seller ID")
    private Long sellerId;

    @Schema(description = "Seller shop name")
    private String sellerShopName;

    @Schema(description = "Order ID this payout is for (nullable for aggregated)")
    private Long orderId;

    @Schema(description = "Order code (if orderId is set)")
    private String orderCode;

    @Schema(description = "Gross order amount before commission")
    private BigDecimal grossAmount;

    @Schema(description = "Platform commission deducted")
    private BigDecimal commissionAmount;

    @Schema(description = "Net amount to be paid to the seller")
    private BigDecimal netAmount;

    @Schema(description = "Current payout status")
    private PayoutStatus status;

    @Schema(description = "Bank transfer reference (filled when PAID)")
    private String bankReference;

    @Schema(description = "When the payout was marked as paid")
    private LocalDateTime paidAt;

    @Schema(description = "Admin user ID who marked paid")
    private Long paidBy;

    @Schema(description = "When the record was created")
    private LocalDateTime createdAt;

    public static SellerPayoutDto from(SellerPayout p) {
        return SellerPayoutDto.builder()
                .id(p.getId())
                .sellerId(p.getSeller() != null ? p.getSeller().getId() : null)
                .sellerShopName(p.getSeller() != null ? p.getSeller().getShopName() : null)
                .orderId(p.getOrderId())
                .grossAmount(p.getGrossAmount())
                .commissionAmount(p.getCommissionAmount())
                .netAmount(p.getNetAmount())
                .status(p.getStatus())
                .bankReference(p.getBankReference())
                .paidAt(p.getPaidAt())
                .paidBy(p.getPaidBy())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
