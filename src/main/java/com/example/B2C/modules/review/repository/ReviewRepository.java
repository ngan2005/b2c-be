package com.example.B2C.modules.review.repository;

import com.example.B2C.modules.review.entity.Review;
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

    Page<Review> findByProductIdAndStatus(Long productId, com.example.B2C.modules.review.entity.ReviewStatus status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'VISIBLE'")
    Double averageRatingByProductId(@Param("productId") Long productId);

    Page<Review> findBySellerIdAndStatus(Long sellerId, com.example.B2C.modules.review.entity.ReviewStatus status, Pageable pageable);
}
