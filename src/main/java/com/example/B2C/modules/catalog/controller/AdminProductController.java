package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.AdminProductSummaryDto;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Products", description = "Admin moderation endpoints for all platform products")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products platform-wide, optionally filtered by status and seller")
    public ResponseEntity<ApiResponse<PageResponse<AdminProductSummaryDto>>> list(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.listProductsForAdmin(status, sellerId, q, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full product detail (admin view)")
    public ResponseEntity<ApiResponse<ProductDetailDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProductForAdmin(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update product status (ACTIVE / HIDDEN / BANNED)")
    public ResponseEntity<ApiResponse<ProductDetailDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusRaw = body.get("status");
        if (statusRaw == null || statusRaw.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("status field is required"));
        }
        ProductStatus newStatus = ProductStatus.valueOf(statusRaw.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(
                "Product status updated", productService.updateProductStatusForAdmin(id, newStatus)));
    }
}
