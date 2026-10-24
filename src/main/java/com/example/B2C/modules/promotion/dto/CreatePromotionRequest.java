package com.example.B2C.modules.promotion.dto;

import com.example.B2C.modules.promotion.entity.DiscountType;
import com.example.B2C.modules.promotion.entity.PromotionApplyScope;
import com.example.B2C.modules.promotion.entity.PromotionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
@Schema(description = "Request payload for creating a promotion/coupon")
public class CreatePromotionRequest {

    @NotBlank(message = "Promotion code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @Schema(description = "Unique coupon/voucher code", example = "SUMMER2024")
    private String code;

    @NotBlank(message = "Promotion name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Schema(description = "Display name of the promotion", example = "Summer Sale 2024")
    private String name;

    @NotNull(message = "Discount type is required")
    @Schema(description = "PERCENT = percentage off, FIXED = fixed amount off, FREESHIP = free shipping",
            example = "PERCENT")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.00", message = "Discount value cannot be negative")
    @Schema(description = "Discount value (interpreted based on discountType)", example = "10.00")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Max discount cannot be negative")
    @Schema(description = "Maximum discount cap (for PERCENT type), null for no cap", example = "50.00")
    private BigDecimal maxDiscountAmount;

    @DecimalMin(value = "0.00", message = "Minimum order value cannot be negative")
    @Schema(description = "Minimum order subtotal required to use this promotion", example = "100000")
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @NotNull(message = "Total quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Total number of times this promotion can be used", example = "100")
    private Integer quantity;

    @Builder.Default
    @Min(value = 1, message = "Limit per user must be at least 1")
    @Schema(description = "Maximum uses per individual user", example = "1")
    private Integer limitPerUser = 1;

    @Builder.Default
    @NotNull(message = "Apply scope is required")
    @Schema(description = "ALL = all products, CATEGORY = specific category, PRODUCT = specific product(s)",
            example = "ALL")
    private PromotionApplyScope applyScope = PromotionApplyScope.ALL;

    @NotNull(message = "Start time is required")
    @Schema(description = "Promotion start timestamp (ISO-8601)", example = "2024-06-01T00:00:00")
    private LocalDateTime startAt;

    @NotNull(message = "End time is required")
    @Schema(description = "Promotion end timestamp (ISO-8601)", example = "2024-08-31T23:59:59")
    private LocalDateTime endAt;

    /**
     * Cross-field validation: the end timestamp must be strictly after the start
     * timestamp. Declared as a method-level constraint so the standard
     * {@code @Valid} cascade in the controller reports it as a normal field
     * error instead of a 400 with a generic message.
     *
     * <p>Previously this rule lived only in the service layer, which meant the
     * seller UI got an opaque "End time must be after start time" toast after
     * a failed round-trip. Validating up-front lets the frontend show the
     * problem inline, next to the offending input.
     */
    @AssertTrue(message = "End time must be after start time")
    @Schema(hidden = true)
    public boolean isDateRangeValid() {
        if (startAt == null || endAt == null) {
            // Individual @NotNull constraints report the missing value.
            return true;
        }
        return endAt.isAfter(startAt);
    }
}
