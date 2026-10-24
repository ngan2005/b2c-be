package com.example.B2C.modules.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add a product variant to the buyer's cart")
public class AddCartItemRequest {

    @NotNull(message = "variantId is required")
    @Schema(description = "ID of the product variant to add", example = "10")
    private Long variantId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Number of units to add (must be ≥ 1)", example = "2")
    private Integer quantity;
}
