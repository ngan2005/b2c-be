package com.example.B2C.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequest {

    @NotNull
    @Min(1)
    private Integer quantity;

    private Boolean isSelected;
}
