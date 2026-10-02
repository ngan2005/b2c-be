package com.example.B2C.modules.catalog.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends BaseRepository<Product, Long> {

    Optional<Product> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlug(String slug);

    /**
     * Search products without a keyword. Splitting this from the keyword search avoids
     * PostgreSQL casting {@code NULL} parameters to {@code bytea} when running
     * {@code LOWER(field) LIKE LOWER(CONCAT('%', :keyword, '%'))}.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND p.status = :status
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:sellerId IS NULL OR p.seller.id = :sellerId)
          AND (:minPrice IS NULL OR p.minPrice >= :minPrice)
          AND (:maxPrice IS NULL OR p.maxPrice <= :maxPrice)
        """)
    Page<Product> searchProductsWithoutKeyword(
            @Param("status") ProductStatus status,
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Search products with a keyword. Only invoked when keyword is non-null and non-blank.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND p.status = :status
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:sellerId IS NULL OR p.seller.id = :sellerId)
          AND (:minPrice IS NULL OR p.minPrice >= :minPrice)
          AND (:maxPrice IS NULL OR p.maxPrice <= :maxPrice)
          AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<Product> searchProductsWithKeyword(
            @Param("status") ProductStatus status,
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND p.status = com.example.B2C.modules.catalog.entity.ProductStatus.ACTIVE
        ORDER BY p.soldCount DESC, p.ratingAvg DESC
        """)
    Page<Product> findFeatured(Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND p.status = com.example.B2C.modules.catalog.entity.ProductStatus.ACTIVE
          AND p.seller.id = :sellerId
        """)
    Page<Product> findActiveBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * Seller-side listing: filter by status (optional) and keyword (optional),
     * restricted to a single seller. Used by the seller dashboard.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND p.seller = :seller
          AND (:status IS NULL OR p.status = :status)
          AND (
            :keyword IS NULL
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<Product> findAllBySeller(
            @Param("seller") com.example.B2C.modules.seller.entity.Seller seller,
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countBySellerIdAndDeletedAtIsNull(Long sellerId);

    long countBySellerIdAndStatusAndDeletedAtIsNull(Long sellerId, ProductStatus status);

    long countByStatusAndDeletedAtIsNull(ProductStatus status);

    long countByDeletedAtIsNull();
}