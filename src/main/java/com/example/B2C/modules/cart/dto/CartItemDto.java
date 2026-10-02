package com.example.B2C.modules.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;
    private Long variantId;
    private String variantName;
    private String productName;
    private String thumbnailUrl;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal lineTotal;
    private Boolean isSelected;
    private LocalDateTime addedAt;
}
