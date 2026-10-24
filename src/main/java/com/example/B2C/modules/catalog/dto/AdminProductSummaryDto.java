package com.example.B2C.modules.catalog.dto;

import com.example.B2C.modules.catalog.entity.ProductStatus;
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
public class AdminProductSummaryDto {

    private Long id;
    private String slug;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String thumbnailUrl;
    private String brand;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer soldCount;
    private Long sellerId;
    private String shopName;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;
    private LocalDateTime createdAt;
}
