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
public class ProductSearchRequest {

    private Long categoryId;
    private Long sellerId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String keyword;
    private String sortBy;
    private String sortDir;
    private Integer page;
    private Integer size;
}
