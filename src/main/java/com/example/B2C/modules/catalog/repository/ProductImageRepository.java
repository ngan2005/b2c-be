package com.example.B2C.modules.catalog.repository;

import com.example.B2C.modules.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("""
        SELECT img FROM ProductImage img
        WHERE img.product.id = :productId
        ORDER BY img.sortOrder ASC, img.id ASC
        """)
    List<ProductImage> findByProductIdOrderBySortOrder(@Param("productId") Long productId);
}
