package com.example.B2C.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCartItemRequest {

    @NotNull
    private Long variantId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
