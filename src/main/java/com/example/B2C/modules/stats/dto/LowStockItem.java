package com.example.B2C.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockItem {
    private Long variantId;
    private String sku;
    private Long productId;
    private String productName;
    private String variantName;
    private Integer stockQuantity;
}
