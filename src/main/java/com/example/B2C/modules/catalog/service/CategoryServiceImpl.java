package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.catalog.dto.CategoryDto;
import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllActiveCategories() {
        return categoryRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getCategoryTree() {
        List<Category> roots = categoryRepository.findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();
        return roots.stream()
                .map(this::toDtoWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug: " + slug));
        return toDtoWithChildren(category);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .level(category.getLevel())
                .path(category.getPath())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }

    private CategoryDto toDtoWithChildren(Category category) {
        CategoryDto dto = toDto(category);
        List<CategoryDto> children = categoryRepository
                .findByParentIdAndIsActiveTrueOrderBySortOrderAsc(category.getId())
                .stream()
                .map(this::toDtoWithChildren)
                .collect(Collectors.toList());
        dto.setChildren(children);
        return dto;
    }
}
