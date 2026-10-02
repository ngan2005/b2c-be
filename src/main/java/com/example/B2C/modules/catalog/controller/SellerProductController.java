package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.catalog.dto.CreateProductRequest;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.dto.UpdateProductRequest;
import com.example.B2C.modules.catalog.dto.UpdateProductStatusRequest;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Products", description = "CRUD endpoints for the authenticated seller's product catalogue")
@SecurityRequirement(name = "bearerAuth")
public class SellerProductController {

    private final ProductService productService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "List my products",
            description = "Returns products owned by the authenticated seller. Optionally filtered by status / keyword.")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDto>>> list(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        PageResponse<ProductSummaryDto> result = productService.listProductsForCurrentSeller(userId, status, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my products by id")
    public ResponseEntity<ApiResponse<ProductDetailDto>> get(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(productService.getProductForSeller(userId, id)));
    }

    @PostMapping
    @Operation(summary = "Create a new product",
            description = "Slug is auto-generated from name unless explicitly provided. New products default to DRAFT.")
    public ResponseEntity<ApiResponse<ProductDetailDto>> create(@Valid @RequestBody CreateProductRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        ProductDetailDto created = productService.createProductForSeller(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one of my products",
            description = "Slug is preserved. Changing status is done via the dedicated PATCH endpoint.")
    public ResponseEntity<ApiResponse<ProductDetailDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Product updated",
                productService.updateProductForSeller(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete one of my products")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        productService.deleteProductForSeller(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change status of one of my products (DRAFT/PENDING/ACTIVE/HIDDEN)")
    public ResponseEntity<ApiResponse<ProductDetailDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        ProductStatus newStatus = ProductStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                productService.updateProductStatusForSeller(userId, id, newStatus)));
    }
}