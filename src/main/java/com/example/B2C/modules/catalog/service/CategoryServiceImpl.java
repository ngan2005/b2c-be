package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.catalog.dto.AdminCategoryDto;
import com.example.B2C.modules.catalog.dto.AdminCategoryRequest;
import com.example.B2C.modules.catalog.dto.AdminCategoryUpdateRequest;
import com.example.B2C.modules.catalog.dto.CategoryDto;
import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
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

    // ===================== Admin-only =====================

    @Override
    public List<AdminCategoryDto> getCategoryTreeForAdmin() {
        List<Category> roots = categoryRepository.findByParentIsNullOrderBySortOrderAsc();
        return roots.stream().map(this::toAdminDtoWithChildren).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminCategoryDto createCategoryForAdmin(AdminCategoryRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName());
        Category parent = null;
        int level = 1;
        String path = "/" + slug;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + request.getParentId()));
            level = (parent.getLevel() != null ? parent.getLevel() : 1) + 1;
            path = (parent.getPath() != null ? parent.getPath() : "") + "/" + slug;
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .parent(parent)
                .iconUrl(request.getIconUrl())
                .level(level)
                .path(path)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(0)
                .build();

        Category saved = categoryRepository.save(category);
        return toAdminDto(saved);
    }

    @Override
    @Transactional
    public AdminCategoryDto updateCategoryForAdmin(Long id, AdminCategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName().trim());
        }
        if (request.getSlug() != null) {
            if (!request.getSlug().isBlank() && !request.getSlug().equals(category.getSlug())) {
                if (categoryRepository.existsBySlug(request.getSlug()) && !request.getSlug().equals(category.getSlug())) {
                    throw new ConflictException("Slug already exists: " + request.getSlug());
                }
                category.setSlug(request.getSlug());
            }
        }
        if (request.getParentId() != null) {
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + request.getParentId()));
            if (newParent.getId().equals(id)) {
                throw new ConflictException("A category cannot be its own parent");
            }
            category.setParent(newParent);
            category.setLevel((newParent.getLevel() != null ? newParent.getLevel() : 1) + 1);
            category.setPath((newParent.getPath() != null ? newParent.getPath() : "") + "/" + category.getSlug());
        }
        if (request.getIconUrl() != null) {
            category.setIconUrl(request.getIconUrl());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category saved = categoryRepository.save(category);
        return toAdminDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategoryForAdmin(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + id));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public AdminCategoryDto toggleCategoryForAdmin(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + id));
        category.setIsActive(!Boolean.TRUE.equals(category.getIsActive()));
        Category saved = categoryRepository.save(category);
        return toAdminDto(saved);
    }

    // ----- Private helpers -----

    private AdminCategoryDto toAdminDto(Category category) {
        return AdminCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .level(category.getLevel())
                .path(category.getPath())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .isActive(category.getIsActive())
                .build();
    }

    private AdminCategoryDto toAdminDtoWithChildren(Category category) {
        AdminCategoryDto dto = toAdminDto(category);
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(category.getId());
        dto.setChildren(children.stream().map(this::toAdminDtoWithChildren).collect(Collectors.toList()));
        return dto;
    }

    private String resolveSlug(String requestedSlug, String name) {
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            String trimmed = requestedSlug.trim();
            if (categoryRepository.existsBySlug(trimmed)) {
                throw new ConflictException("Slug already exists: " + trimmed);
            }
            return trimmed;
        }
        String base = slugify(name == null ? "" : name);
        if (base.isEmpty()) base = "category";
        String candidate = base;
        int suffix = 1;
        while (categoryRepository.existsBySlug(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private static String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String stripped = NON_LATIN.matcher(normalized).replaceAll("");
        return stripped.toLowerCase(Locale.ENGLISH);
    }
}
