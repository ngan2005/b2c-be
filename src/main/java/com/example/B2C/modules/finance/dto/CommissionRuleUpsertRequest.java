package com.example.B2C.modules.finance.dto;

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
@Schema(description = "Request payload for creating or updating a commission rule")
public class CommissionRuleUpsertRequest {

    @Schema(description = "Category ID; null to create/edit a global default rule", example = "3")
    private Long categoryId;

    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must be at most 150 characters")
    @Schema(description = "Display name for the rule", example = "Electronics commission 7%")
    private String name;

    @NotNull(message = "percent is required")
    @DecimalMin(value = "0.00", message = "percent must be >= 0")
    @DecimalMax(value = "100.00", message = "percent must be <= 100")
    @Schema(description = "Commission rate as percentage, e.g. 7.00 = 7%", example = "7.00")
    private BigDecimal percent;

    @DecimalMin(value = "0.00", message = "minOrderValue must be >= 0")
    @Schema(description = "Minimum item total_price to apply this rule; default 0", example = "50000")
    private BigDecimal minOrderValue;

    @DecimalMin(value = "0.00", message = "maxCommission must be >= 0")
    @Schema(description = "Maximum commission cap per order item; null = no cap", example = "100000")
    private BigDecimal maxCommission;

    @Schema(description = "Whether the rule is active on creation; default true", example = "true")
    private Boolean isActive;

    @Schema(description = "When the rule becomes effective; defaults to now", example = "2024-01-01T00:00:00")
    private LocalDateTime effectiveFrom;

    @Schema(description = "When the rule expires; null = open-ended", example = "2024-12-31T23:59:59")
    private LocalDateTime effectiveTo;
}
