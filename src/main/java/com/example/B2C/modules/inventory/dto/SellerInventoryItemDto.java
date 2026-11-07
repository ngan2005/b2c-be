package com.example.B2C.modules.inventory.dto;

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
public class SellerInventoryItemDto {
    private Long variantId;
    private Long productId;
    private String productName;
    private String productSlug;
    private String variantName;
    private String sku;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableStock;
    private Integer soldCount;
    private String productStatus;
    private Boolean isActive;
    private LocalDateTime updatedAt;
}
