package com.example.B2C.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {

    private Long id;
    private String sku;
    private String variantName;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private Integer soldCount;
    private String imageUrl;
    private Integer weightGram;
    private String barcode;
    private Boolean isActive;
}
