package com.example.B2C.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {

    private Long id;
    private String slug;
    private String name;
    private String description;
    private String brand;

    private Long sellerId;
    private String shopName;
    private String shopSlug;
    private BigDecimal sellerRatingAvg;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private String thumbnailUrl;
    private List<ProductImageDto> images;

    private List<ProductVariantDto> variants;
    private List<ProductOptionDto> options;

    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer soldCount;
    private Long viewCount;

    private Integer weightGram;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
