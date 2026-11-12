package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.CategoryDto;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.service.CategoryService;
import com.example.B2C.modules.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Public category browsing APIs")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all active categories (flat)")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllActiveCategories()));
    }

    @GetMapping("/roots")
    @Operation(summary = "List top-level (root) categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getRootCategories()));
    }

    @GetMapping("/tree")
    @Operation(summary = "List all categories as a tree (with nested children)")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryTree()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a category by its slug, including children tree")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryBySlug(slug)));
    }

    @GetMapping("/{id}/products")
    @Operation(summary = "Get products belonging to a category (including subcategories)")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDto>>> getCategoryProducts(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "created") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .categoryId(id)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(request)));
    }
}
