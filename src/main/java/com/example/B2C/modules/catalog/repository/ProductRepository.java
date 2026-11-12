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
     * Filter-only search: no keyword, just category/seller/price/status filters.
     * No FTS involved — pure indexed lookups.
     */
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN p.category c
        LEFT JOIN c.parent parent
        WHERE p.deletedAt IS NULL
          AND p.status = :status
          AND (
            :categoryId IS NULL 
            OR c.id = :categoryId 
            OR parent.id = :categoryId
          )
          AND (:sellerId IS NULL OR p.seller.id = :sellerId)
          AND (:minPrice IS NULL OR p.minPrice >= :minPrice)
          AND (:maxPrice IS NULL OR p.maxPrice <= :maxPrice)
          AND (:brand IS NULL OR p.brand = :brand)
          AND (:minRating IS NULL OR p.ratingAvg >= :minRating)
        """)
    Page<Product> searchProductsWithoutKeyword(
            @Param("status") ProductStatus status,
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("brand") String brand,
            @Param("minRating") BigDecimal minRating,
            Pageable pageable
    );

    /**
     * Keyword search using LIKE (case-insensitive across name, description, brand).
     * Temporary implementation as requested (will be upgraded to FTS later).
     */
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN p.category c
        LEFT JOIN c.parent parent
        WHERE p.deletedAt IS NULL
          AND p.status = :status
          AND (
            :categoryId IS NULL 
            OR c.id = :categoryId 
            OR parent.id = :categoryId
          )
          AND (:sellerId IS NULL OR p.seller.id = :sellerId)
          AND (:minPrice IS NULL OR p.minPrice >= :minPrice)
          AND (:maxPrice IS NULL OR p.maxPrice <= :maxPrice)
          AND (:brand IS NULL OR p.brand = :brand)
          AND (:minRating IS NULL OR p.ratingAvg >= :minRating)
          AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<Product> searchProductsWithKeywordLike(
            @Param("status") ProductStatus status,
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("brand") String brand,
            @Param("minRating") BigDecimal minRating,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Full-text search on products via tsvector with prefix support and trigram fallback.
     *
     * @param status     product status filter (optional)
     * @param categoryId optional category filter
     * @param sellerId   optional seller filter
     * @param minPrice   optional min price
     * @param maxPrice   optional max price
     * @param brand      optional brand filter
     * @param minRating  optional min rating
     * @param q          raw search string for ILIKE matching
     * @param tsQuery    formatted tsquery string with prefix wildcard (:*)
     * @param pageable   pagination
     * @return matching products ordered by relevance then recency
     */
    @Query(value = """
        SELECT p.* FROM product p
        LEFT JOIN category c ON p.category_id = c.id
        WHERE p.deleted_at IS NULL
          AND (:status IS NULL OR p.status = :status)
          AND (
            CAST(:categoryId AS BIGINT) IS NULL 
            OR p.category_id = CAST(:categoryId AS BIGINT)
            OR c.parent_id = CAST(:categoryId AS BIGINT)
          )
          AND (:sellerId IS NULL OR p.seller_id = :sellerId)
          AND (:minPrice IS NULL OR p.min_price >= :minPrice)
          AND (:maxPrice IS NULL OR p.max_price <= :maxPrice)
          AND (:brand IS NULL OR p.brand = :brand)
          AND (:minRating IS NULL OR p.rating_avg >= :minRating)
          AND (
            (:tsQuery IS NOT NULL AND p.search_vector @@ to_tsquery('simple', :tsQuery))
            OR p.name ILIKE '%' || :q || '%'
            OR p.brand ILIKE '%' || :q || '%'
            OR p.description ILIKE '%' || :q || '%'
          )
        ORDER BY
          CASE 
            WHEN :tsQuery IS NOT NULL AND p.search_vector @@ to_tsquery('simple', :tsQuery)
            THEN ts_rank(p.search_vector, to_tsquery('simple', :tsQuery))
            ELSE 0 
          END DESC,
          p.created_at DESC
        """, nativeQuery = true,
        countQuery = """
        SELECT count(p.id) FROM product p
        LEFT JOIN category c ON p.category_id = c.id
        WHERE p.deleted_at IS NULL
          AND (:status IS NULL OR p.status = :status)
          AND (
            CAST(:categoryId AS BIGINT) IS NULL 
            OR p.category_id = CAST(:categoryId AS BIGINT)
            OR c.parent_id = CAST(:categoryId AS BIGINT)
          )
          AND (:sellerId IS NULL OR p.seller_id = :sellerId)
          AND (:minPrice IS NULL OR p.min_price >= :minPrice)
          AND (:maxPrice IS NULL OR p.max_price <= :maxPrice)
          AND (:brand IS NULL OR p.brand = :brand)
          AND (:minRating IS NULL OR p.rating_avg >= :minRating)
          AND (
            (:tsQuery IS NOT NULL AND p.search_vector @@ to_tsquery('simple', :tsQuery))
            OR p.name ILIKE '%' || :q || '%'
            OR p.brand ILIKE '%' || :q || '%'
            OR p.description ILIKE '%' || :q || '%'
          )
        """)
    Page<Product> searchProductsFulltext(
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("brand") String brand,
            @Param("minRating") BigDecimal minRating,
            @Param("q") String q,
            @Param("tsQuery") String tsQuery,
            Pageable pageable
    );

    /**
     * Seller-side full-text search.
     */
    @Query(value = """
        SELECT p.* FROM product p
        WHERE p.deleted_at IS NULL
          AND p.seller_id = :sellerId
          AND (:status IS NULL OR p.status = :status)
          AND (
            (:tsQuery IS NOT NULL AND p.search_vector @@ to_tsquery('simple', :tsQuery))
            OR p.name ILIKE '%' || :q || '%'
            OR p.brand ILIKE '%' || :q || '%'
            OR p.description ILIKE '%' || :q || '%'
          )
        ORDER BY
          CASE 
            WHEN :tsQuery IS NOT NULL AND p.search_vector @@ to_tsquery('simple', :tsQuery)
            THEN ts_rank(p.search_vector, to_tsquery('simple', :tsQuery))
            ELSE 0 
          END DESC,
          p.created_at DESC
        """, nativeQuery = true,
        countQuery = """
        SELECT count(p.id) FROM product p
        WHERE p.deleted_at IS NULL
          AND p.seller_id = :sellerId
          AND (:status IS NULL OR p.status = :status)
          AND (
            (:tsQuery IS NOT NULL AND p.search_vector @@ to_tsquery('simple', :tsQuery))
            OR p.name ILIKE '%' || :q || '%'
            OR p.brand ILIKE '%' || :q || '%'
            OR p.description ILIKE '%' || :q || '%'
          )
        """)
    Page<Product> searchProductsSellerFulltext(
            @Param("sellerId") Long sellerId,
            @Param("status") String status,
            @Param("q") String q,
            @Param("tsQuery") String tsQuery,
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
     * Seller-side listing without a keyword. No changes here — no LIKE involved.
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND p.seller = :seller
          AND (:status IS NULL OR p.status = :status)
        """)
    Page<Product> findAllBySellerWithoutKeyword(
            @Param("seller") com.example.B2C.modules.seller.entity.Seller seller,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    long countBySellerIdAndDeletedAtIsNull(Long sellerId);

    long countBySellerIdAndStatusAndDeletedAtIsNull(Long sellerId, ProductStatus status);

    long countByStatusAndDeletedAtIsNull(ProductStatus status);

    long countByDeletedAtIsNull();

    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
        """)
    org.springframework.data.domain.Page<Product> findAllDeletedAtIsNull(org.springframework.data.domain.Pageable pageable);
}