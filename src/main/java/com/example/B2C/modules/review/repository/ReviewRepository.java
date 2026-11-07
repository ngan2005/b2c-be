package com.example.B2C.modules.review.repository;

import com.example.B2C.modules.review.entity.Review;
import com.example.B2C.modules.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.status = :status AND (:rating IS NULL OR r.rating = :rating)")
    Page<Review> findByProductIdAndStatusWithOptionalRating(
            @Param("productId") Long productId,
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating,
            Pageable pageable
    );

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'VISIBLE'")
    Double averageRatingByProductId(@Param("productId") Long productId);

    Page<Review> findBySellerIdAndStatus(Long sellerId, ReviewStatus status, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:rating IS NULL OR r.rating = :rating)")
    Page<Review> findForAdminWithoutKeyword(
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating,
            Pageable pageable
    );

    @Query("SELECT r FROM Review r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:rating IS NULL OR r.rating = :rating) AND " +
           "(LOWER(r.comment) LIKE :pattern " +
           " OR LOWER(r.user.fullName) LIKE :pattern " +
           " OR LOWER(r.product.name) LIKE :pattern " +
           " OR LOWER(r.seller.shopName) LIKE :pattern)")
    Page<Review> findForAdminWithKeyword(
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating,
            @Param("pattern") String pattern,
            Pageable pageable
    );

    long countByStatus(ReviewStatus status);

    long countByRating(Integer rating);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.status = 'VISIBLE'")
    Double averagePlatformRating();
}
