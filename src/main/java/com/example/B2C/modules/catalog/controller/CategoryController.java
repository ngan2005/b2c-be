package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.catalog.dto.CategoryDto;
import com.example.B2C.modules.catalog.service.CategoryService;
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
}
