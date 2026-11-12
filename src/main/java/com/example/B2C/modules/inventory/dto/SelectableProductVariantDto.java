package com.example.B2C.modules.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectableProductVariantDto {
    private Long variantId;
    private Long productId;
    private String productName;
    private String variantName;
    private String sku;
    private String imageUrl;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableStock;
    private String shopName;
}

