package com.example.B2C.modules.catalog.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends BaseRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("""
        SELECT v FROM ProductVariant v
        WHERE v.product.id = :productId AND v.isActive = true
        ORDER BY v.price ASC
        """)
    List<ProductVariant> findActiveByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT v FROM ProductVariant v
        WHERE v.product.id = :productId
        ORDER BY v.price ASC
        """)
    List<ProductVariant> findAllByProductId(@Param("productId") Long productId);
}
