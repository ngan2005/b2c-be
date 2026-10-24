package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.catalog.dto.AdminCategoryDto;
import com.example.B2C.modules.catalog.dto.AdminCategoryRequest;
import com.example.B2C.modules.catalog.dto.AdminCategoryUpdateRequest;
import com.example.B2C.modules.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Categories", description = "Admin CRUD for platform categories")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "Get full category tree including inactive nodes")
    public ResponseEntity<ApiResponse<List<AdminCategoryDto>>> getTree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryTreeForAdmin()));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<AdminCategoryDto>> create(
            @Valid @RequestBody AdminCategoryRequest request) {
        AdminCategoryDto created = categoryService.createCategoryForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update of a category")
    public ResponseEntity<ApiResponse<AdminCategoryDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.updateCategoryForAdmin(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a category (sets isActive=false). Fails 409 if category has products.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategoryForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Category deactivated", null));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle category active/inactive status")
    public ResponseEntity<ApiResponse<AdminCategoryDto>> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.toggleCategoryForAdmin(id)));
    }
}
