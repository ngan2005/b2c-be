package com.example.B2C.modules.catalog.repository;

import com.example.B2C.modules.catalog.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

    @Query("""
            SELECT v FROM ProductOptionValue v
            WHERE v.option.id IN :optionIds
            ORDER BY v.sortOrder ASC, v.id ASC
            """)
    List<ProductOptionValue> findByOptionIds(@Param("optionIds") List<Long> optionIds);

    List<ProductOptionValue> findByOptionIdOrderBySortOrderAsc(Long optionId);

    // P1.3 — fetch option-value IDs associated with a specific variant
    @Query("""
            SELECT vov.optionValue FROM VariantOptionValue vov
            JOIN FETCH vov.optionValue.option
            WHERE vov.variant.id = :variantId
            ORDER BY vov.optionValue.id ASC
            """)
    List<ProductOptionValue> findByVariantId(@Param("variantId") Long variantId);
}
