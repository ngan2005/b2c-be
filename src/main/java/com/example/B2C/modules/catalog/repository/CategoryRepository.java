package com.example.B2C.modules.catalog.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.catalog.entity.Category;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends BaseRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();

    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findAllActive();

    boolean existsBySlug(String slug);
}
