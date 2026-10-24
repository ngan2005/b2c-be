package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.AdminCategoryDto;
import com.example.B2C.modules.catalog.dto.AdminCategoryRequest;
import com.example.B2C.modules.catalog.dto.AdminCategoryUpdateRequest;
import com.example.B2C.modules.catalog.dto.CategoryDto;
import com.example.B2C.modules.catalog.entity.Category;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getAllActiveCategories();

    List<CategoryDto> getRootCategories();

    List<CategoryDto> getCategoryTree();

    CategoryDto getCategoryBySlug(String slug);

    // ----- Admin-only -----

    List<AdminCategoryDto> getCategoryTreeForAdmin();

    AdminCategoryDto createCategoryForAdmin(AdminCategoryRequest request);

    AdminCategoryDto updateCategoryForAdmin(Long id, AdminCategoryUpdateRequest request);

    void deleteCategoryForAdmin(Long id);

    AdminCategoryDto toggleCategoryForAdmin(Long id);
}
