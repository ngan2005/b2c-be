package com.example.B2C.modules.catalog.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends BaseRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    /**
     * Acquire a row-level write lock (SELECT ... FOR UPDATE) on the variant.
     * Used by the E2 pessimistic locking strategy.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);

    /**
     * Read-only lock — used for non-mutating queries that still want a consistent
     * snapshot under high contention (E2 can also use this for read paths).
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdForShare(@Param("id") Long id);

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

    /**
     * Count low-stock variants of a seller. "Low stock" means stock below the
     * threshold AND the product itself is not hidden/deleted.
     */
    @Query("""
        SELECT COUNT(v) FROM ProductVariant v
        WHERE v.product.seller.id = :sellerId
          AND v.product.deletedAt IS NULL
          AND v.product.status <> :hidden
          AND v.isActive = true
          AND v.stockQuantity <= :threshold
        """)
    long countLowStockForSeller(@Param("sellerId") Long sellerId,
                                @Param("hidden") ProductStatus hidden,
                                @Param("threshold") int threshold);
}