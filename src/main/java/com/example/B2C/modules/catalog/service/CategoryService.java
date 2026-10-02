package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.CategoryDto;
import com.example.B2C.modules.catalog.entity.Category;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getAllActiveCategories();

    List<CategoryDto> getRootCategories();

    List<CategoryDto> getCategoryTree();

    CategoryDto getCategoryBySlug(String slug);
}
