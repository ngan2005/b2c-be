package com.example.B2C.modules.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of applying a promotion code — shows computed discount")
public class ApplyPromotionResponse {

    @Schema(description = "Promotion code applied", example = "SUMMER2024")
    private String code;

    @Schema(description = "Name of the applied promotion", example = "Summer Sale 2024")
    private String name;

    @Schema(description = "Discount type applied", example = "PERCENT")
    private String discountType;

    @Schema(description = "Computed discount amount deducted from order total", example = "20000")
    private BigDecimal discountAmount;

    @Schema(description = "Remaining platform credit or coupon balance after applying (if applicable)", example = "0")
    private BigDecimal remainingBalance;

    @Schema(description = "Whether the promotion was successfully applied", example = "true")
    private Boolean success;

    @Schema(description = "Error message if application failed", example = "Minimum order value not met")
    private String message;
}
