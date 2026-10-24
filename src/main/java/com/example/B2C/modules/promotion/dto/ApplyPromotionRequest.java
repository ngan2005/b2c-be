package com.example.B2C.modules.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to apply (redeem) a promotion code at checkout")
public class ApplyPromotionRequest {

    @NotBlank(message = "Promotion code is required")
    @Schema(description = "The coupon/promotion code to apply", example = "SUMMER2024")
    private String code;

    @Schema(description = "Order subtotal (used to verify minOrderValue and compute discount)",
            example = "200000")
    private java.math.BigDecimal subtotal;
}
