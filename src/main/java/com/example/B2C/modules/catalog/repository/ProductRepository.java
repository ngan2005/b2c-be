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
     * Full-text search on products via tsvector.
     *
     * Uses websearch_to_tsquery so the caller can pass plain words (ANDed),
     * quoted phrases (exact match), -exclusion, or bare words (ORed in websearch mode).
     * The trigram index on name/description acts as a secondary plan for very short
     * queries (&lt; 2 chars) where websearch_to_tsquery has no result.
     *
     * @param status     product status filter
     * @param categoryId optional category filter
     * @param sellerId   optional seller filter
     * @param minPrice   optional min price
     * @param maxPrice   optional max price
     * @param q          raw search string — passed verbatim to websearch_to_tsquery
     * @param pageable   pagination
     * @return matching products ordered by relevance then recency
     */
    @Query(value = """
        SELECT p.* FROM product p
        WHERE p.deleted_at IS NULL
          AND p.status = :status
          AND (:categoryId IS NULL OR p.category_id = :categoryId)
          AND (:sellerId IS NULL OR p.seller_id = :sellerId)
          AND (:minPrice IS NULL OR p.min_price >= :minPrice)
          AND (:maxPrice IS NULL OR p.max_price <= :maxPrice)
          AND (
            p.search_vector @@ websearch_to_tsquery('simple', public.immutable_unaccent(:q))
            OR (:q_len < 2 AND (
                p.name ILIKE '%' || :q || '%'
                OR p.description ILIKE '%' || :q || '%'
            ))
          )
        ORDER BY
          ts_rank(p.search_vector, websearch_to_tsquery('simple', public.immutable_unaccent(:q))) DESC,
          p.created_at DESC
        """, nativeQuery = true,
        countQuery = """
        SELECT count(p.id) FROM product p
        WHERE p.deleted_at IS NULL
          AND p.status = :status
          AND (:categoryId IS NULL OR p.category_id = :categoryId)
          AND (:sellerId IS NULL OR p.seller_id = :sellerId)
          AND (:minPrice IS NULL OR p.min_price >= :minPrice)
          AND (:maxPrice IS NULL OR p.max_price <= :maxPrice)
          AND (
            p.search_vector @@ websearch_to_tsquery('simple', public.immutable_unaccent(:q))
            OR (:q_len < 2 AND (
                p.name ILIKE '%' || :q || '%'
                OR p.description ILIKE '%' || :q || '%'
            ))
          )
        """)
    Page<Product> searchProductsFulltext(
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("sellerId") Long sellerId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("q") String q,
            @Param("q_len") int qLen,
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
            p.search_vector @@ websearch_to_tsquery('simple', public.immutable_unaccent(:q))
            OR (:q_len < 2 AND (
                p.name ILIKE '%' || :q || '%'
                OR p.description ILIKE '%' || :q || '%'
            ))
          )
        ORDER BY
          ts_rank(p.search_vector, websearch_to_tsquery('simple', public.immutable_unaccent(:q))) DESC,
          p.created_at DESC
        """, nativeQuery = true,
        countQuery = """
        SELECT count(p.id) FROM product p
        WHERE p.deleted_at IS NULL
          AND p.seller_id = :sellerId
          AND (:status IS NULL OR p.status = :status)
          AND (
            p.search_vector @@ websearch_to_tsquery('simple', public.immutable_unaccent(:q))
            OR (:q_len < 2 AND (
                p.name ILIKE '%' || :q || '%'
                OR p.description ILIKE '%' || :q || '%'
            ))
          )
        """)
    Page<Product> searchProductsSellerFulltext(
            @Param("sellerId") Long sellerId,
            @Param("status") String status,
            @Param("q") String q,
            @Param("q_len") int qLen,
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