package com.example.B2C.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    // P1.3 — IDs of the option values (e.g. colour, size) assigned to this variant
    private List<Long> optionValueIds;

    // Key-value attributes: e.g. {"Màu sắc": "Đỏ", "Kích thước": "XL"}
    private Map<String, String> attributes;
}
