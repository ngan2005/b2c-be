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
@Schema(description = "Request to update quantity or selection of a cart item")
public class UpdateCartItemRequest {

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "New quantity (set to the desired count; pass the absolute value, not a delta)",
            example = "3")
    private Integer quantity;

    @Schema(description = "Whether this item is selected for checkout", example = "true")
    private Boolean isSelected;
}
