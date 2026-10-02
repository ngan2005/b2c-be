package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.CreateProductRequest;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.dto.UpdateProductRequest;
import com.example.B2C.modules.catalog.entity.ProductStatus;

public interface ProductService {

    PageResponse<ProductSummaryDto> searchProducts(ProductSearchRequest request);

    ProductDetailDto getProductDetail(String slug);

    PageResponse<ProductSummaryDto> getFeaturedProducts(int page, int size);

    PageResponse<ProductSummaryDto> getProductsBySeller(Long sellerId, int page, int size);

    void incrementViewCount(Long productId);

    // ----- Seller-only product management -----

    PageResponse<ProductSummaryDto> listProductsForCurrentSeller(Long sellerId, ProductStatus status,
                                                                String keyword, int page, int size);

    ProductDetailDto getProductForSeller(Long sellerId, Long productId);

    ProductDetailDto createProductForSeller(Long sellerId, CreateProductRequest request);

    ProductDetailDto updateProductForSeller(Long sellerId, Long productId, UpdateProductRequest request);

    void deleteProductForSeller(Long sellerId, Long productId);

    ProductDetailDto updateProductStatusForSeller(Long sellerId, Long productId, ProductStatus status);
}