package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product browsing APIs (no authentication required)")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
            summary = "Search and list active products",
            description = "Supports filtering by category, seller, price range, keyword search. Sortable and paginated."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters")
    })
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDto>>> searchProducts(
            @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Seller/Shop ID") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "Minimum price (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Search keyword in name/description/brand") @RequestParam(required = false) String q,
            @Parameter(description = "Sort field: price | sold | rating | created | name") @RequestParam(required = false, defaultValue = "created") String sortBy,
            @Parameter(description = "Sort direction: asc | desc") @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @Parameter(description = "Zero-based page index") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100, default 20)") @RequestParam(required = false, defaultValue = "20") int size
    ) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .categoryId(categoryId)
                .sellerId(sellerId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .keyword(q)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .page(page)
                .size(size)
                .build();

        PageResponse<ProductSummaryDto> result = productService.searchProducts(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/featured")
    @Operation(summary = "List featured products", description = "Sorted by sold count descending")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDto>>> getFeaturedProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PageResponse<ProductSummaryDto> result = productService.getFeaturedProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List all active products of a seller")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDto>>> getProductsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PageResponse<ProductSummaryDto> result = productService.getProductsBySeller(sellerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{slug}")
    @Operation(
            summary = "Get product detail by slug",
            description = "Returns full product info including variants, options, images, ratings"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found or hidden")
    })
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetail(@PathVariable String slug) {
        ProductDetailDto product = productService.getProductDetail(slug);
        productService.incrementViewCount(product.getId());
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
