package com.example.B2C.modules.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * A category with its total GMV from completed orders.
 */
@Schema(description = "Top category by GMV")
public record TopCategoryDto(
        @Schema(description = "Category ID", example = "3")
        Long id,

        @Schema(description = "Category name", example = "Electronics")
        String name,

        @Schema(description = "Total GMV from this category (COMPLETED orders)", example = "25000000")
        BigDecimal gmv
) {
}
