package com.example.B2C.modules.promotion.dto;

import com.example.B2C.modules.promotion.entity.DiscountType;
import com.example.B2C.modules.promotion.entity.Promotion;
import com.example.B2C.modules.promotion.entity.PromotionApplyScope;
import com.example.B2C.modules.promotion.entity.PromotionStatus;
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
@Schema(description = "Promotion/coupon response payload")
public class PromotionDto {

    @Schema(description = "Promotion record ID", example = "1")
    private Long id;

    @Schema(description = "Seller ID (null for platform-wide promotions)", example = "5")
    private Long sellerId;

    @Schema(description = "Unique coupon code", example = "SUMMER2024")
    private String code;

    @Schema(description = "Display name", example = "Summer Sale 2024")
    private String name;

    @Schema(description = "DISCOUNT_TYPE enum value", example = "PERCENT")
    private DiscountType discountType;

    @Schema(description = "Discount value", example = "10.00")
    private BigDecimal discountValue;

    @Schema(description = "Maximum discount cap (null = no cap)", example = "50.00")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Minimum order subtotal required", example = "100000")
    private BigDecimal minOrderValue;

    @Schema(description = "Total available quantity", example = "100")
    private Integer quantity;

    @Schema(description = "Number of times already redeemed", example = "23")
    private Integer usedCount;

    @Schema(description = "Remaining quantity (quantity - usedCount)", example = "77")
    private Integer remainingQuantity;

    @Schema(description = "Max uses per user", example = "1")
    private Integer limitPerUser;

    @Schema(description = "APPLY_SCOPE enum value", example = "ALL")
    private PromotionApplyScope applyScope;

    @Schema(description = "Start timestamp", example = "2024-06-01T00:00:00")
    private LocalDateTime startAt;

    @Schema(description = "End timestamp", example = "2024-08-31T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "PROMOTION_STATUS enum value", example = "ACTIVE")
    private PromotionStatus status;

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-05-15T10:00:00")
    private LocalDateTime createdAt;

    public static PromotionDto from(Promotion p) {
        return PromotionDto.builder()
                .id(p.getId())
                .sellerId(p.getSeller() != null ? p.getSeller().getId() : null)
                .code(p.getCode())
                .name(p.getName())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .maxDiscountAmount(p.getMaxDiscountAmount())
                .minOrderValue(p.getMinOrderValue())
                .quantity(p.getQuantity())
                .usedCount(p.getUsedCount())
                .remainingQuantity(p.getQuantity() - p.getUsedCount())
                .limitPerUser(p.getLimitPerUser())
                .applyScope(p.getApplyScope())
                .startAt(p.getStartAt())
                .endAt(p.getEndAt())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
