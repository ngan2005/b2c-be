package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.CommissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Commission record response payload")
public class CommissionRecordDto {

    @Schema(description = "Record ID", example = "1")
    private Long id;

    @Schema(description = "Order ID", example = "100")
    private Long orderId;

    @Schema(description = "Order item ID this record is for", example = "500")
    private Long orderItemId;

    @Schema(description = "Seller ID who fulfilled the order", example = "7")
    private Long sellerId;

    @Schema(description = "Category ID of the product", example = "3")
    private Long categoryId;

    @Schema(description = "Category name for display", example = "Electronics")
    private String categoryName;

    @Schema(description = "Commission rule ID that was applied; null if fallback", example = "2")
    private Long ruleId;

    @Schema(description = "Order item total_price used for calculation", example = "150000.00")
    private BigDecimal orderAmount;

    @Schema(description = "Commission rate that was applied", example = "7.00")
    private BigDecimal commissionRate;

    @Schema(description = "Calculated commission amount", example = "10500.00")
    private BigDecimal commissionAmount;

    @Schema(description = "Net amount after commission (order_amount - commission)", example = "139500.00")
    private BigDecimal sellerNet;

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-06-01T12:00:00")
    private LocalDateTime createdAt;

    public static CommissionRecordDto from(CommissionRecord r) {
        return CommissionRecordDto.builder()
                .id(r.getId())
                .orderId(r.getOrder().getId())
                .orderItemId(r.getOrderItem().getId())
                .sellerId(r.getSeller().getId())
                .categoryId(r.getCategory() != null ? r.getCategory().getId() : null)
                .categoryName(r.getCategory() != null ? r.getCategory().getName() : null)
                .ruleId(r.getRule() != null ? r.getRule().getId() : null)
                .orderAmount(r.getOrderAmount())
                .commissionRate(r.getCommissionRate())
                .commissionAmount(r.getCommissionAmount())
                .sellerNet(r.getSellerNet())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
