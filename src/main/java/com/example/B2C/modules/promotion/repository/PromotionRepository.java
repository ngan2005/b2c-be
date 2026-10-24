package com.example.B2C.modules.promotion.repository;

import com.example.B2C.modules.promotion.entity.Promotion;
import com.example.B2C.modules.promotion.entity.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Uniqueness check that ignores a specific row — used when editing a
     * promotion so that keeping its own code does not trip the constraint.
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT p FROM Promotion p WHERE p.deletedAt IS NULL")
    Page<Promotion> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE p.deletedAt IS NULL AND p.status = :status")
    Page<Promotion> findByStatusActive(@Param("status") PromotionStatus status, Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE p.deletedAt IS NULL AND p.seller.id = :sellerId")
    Page<Promotion> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE p.deletedAt IS NULL AND p.seller.id = :sellerId AND p.status = :status")
    Page<Promotion> findBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                            @Param("status") PromotionStatus status,
                                            Pageable pageable);

    /**
     * Returns promotions applicable to the given seller or platform-wide (seller_id IS NULL),
     * that are active at the given time and still have available quantity.
     */
    @Query("""
            SELECT p FROM Promotion p
            WHERE (p.seller.id = :sellerId OR p.seller IS NULL)
              AND p.status = 'ACTIVE'
              AND p.startAt <= :now
              AND p.endAt >= :now
              AND p.quantity > p.usedCount
            ORDER BY p.endAt ASC
            """)
    List<Promotion> findActiveForSeller(@Param("sellerId") Long sellerId, @Param("now") LocalDateTime now);

    @Query("""
            SELECT p FROM Promotion p
            WHERE p.code = :code
              AND (p.seller.id = :sellerId OR p.seller IS NULL)
              AND p.status = 'ACTIVE'
              AND p.startAt <= :now
              AND p.endAt >= :now
              AND p.quantity > p.usedCount
            """)
    Optional<Promotion> findValidByCode(@Param("code") String code,
                                         @Param("sellerId") Long sellerId,
                                         @Param("now") LocalDateTime now);

    @Query("""
            SELECT p FROM Promotion p
            WHERE p.status = 'ACTIVE'
              AND p.startAt <= :now
              AND p.endAt >= :now
              AND p.quantity > p.usedCount
            ORDER BY p.endAt ASC
            """)
    List<Promotion> findAllActivePromotions(@Param("now") LocalDateTime now);

    Page<Promotion> findByStatus(PromotionStatus status, Pageable pageable);
}
