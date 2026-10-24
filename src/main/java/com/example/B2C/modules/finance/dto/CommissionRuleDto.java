package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.CommissionRule;
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
@Schema(description = "Commission rule response payload")
public class CommissionRuleDto {

    @Schema(description = "Rule record ID", example = "1")
    private Long id;

    @Schema(description = "Category ID this rule applies to; null means global default", example = "3")
    private Long categoryId;

    @Schema(description = "Category name for display", example = "Electronics")
    private String categoryName;

    @Schema(description = "Rule display name", example = "Electronics commission 7%")
    private String name;

    @Schema(description = "Commission rate as percentage", example = "7.00")
    private BigDecimal percent;

    @Schema(description = "Minimum order item value to apply this rule; 0 = no minimum", example = "50000")
    private BigDecimal minOrderValue;

    @Schema(description = "Maximum commission per order item; null = no cap", example = "100000")
    private BigDecimal maxCommission;

    @Schema(description = "Whether the rule is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "When this rule becomes effective", example = "2024-01-01T00:00:00")
    private LocalDateTime effectiveFrom;

    @Schema(description = "When this rule expires; null = open-ended", example = "2024-12-31T23:59:59")
    private LocalDateTime effectiveTo;

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    public static CommissionRuleDto from(CommissionRule r) {
        return CommissionRuleDto.builder()
                .id(r.getId())
                .categoryId(r.getCategory() != null ? r.getCategory().getId() : null)
                .categoryName(r.getCategory() != null ? r.getCategory().getName() : null)
                .name(r.getName())
                .percent(r.getPercent())
                .minOrderValue(r.getMinOrderValue())
                .maxCommission(r.getMaxCommission())
                .isActive(r.getIsActive())
                .effectiveFrom(r.getEffectiveFrom())
                .effectiveTo(r.getEffectiveTo())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
