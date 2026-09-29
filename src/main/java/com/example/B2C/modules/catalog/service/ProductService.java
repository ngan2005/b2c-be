package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;

public interface ProductService {

    PageResponse<ProductSummaryDto> searchProducts(ProductSearchRequest request);

    ProductDetailDto getProductDetail(String slug);

    PageResponse<ProductSummaryDto> getFeaturedProducts(int page, int size);

    PageResponse<ProductSummaryDto> getProductsBySeller(Long sellerId, int page, int size);

    void incrementViewCount(Long productId);
}
