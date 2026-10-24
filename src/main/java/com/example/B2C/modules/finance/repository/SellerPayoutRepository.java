package com.example.B2C.modules.finance.repository;

import com.example.B2C.modules.finance.entity.PayoutStatus;
import com.example.B2C.modules.finance.entity.SellerPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerPayoutRepository extends JpaRepository<SellerPayout, Long> {

    Page<SellerPayout> findByStatus(PayoutStatus status, Pageable pageable);

    List<SellerPayout> findBySellerIdAndStatus(Long sellerId, PayoutStatus status);

    /**
     * Returns true if a payout record already exists for this order that is NOT cancelled.
     * Used for idempotency when creating payout for a completed order.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM SellerPayout p WHERE p.orderId = :orderId AND p.status <> com.example.B2C.modules.finance.entity.PayoutStatus.CANCELLED")
    boolean existsByOrderIdAndStatusNot(@Param("orderId") Long orderId, PayoutStatus excludedStatus);

    /**
     * Count PENDING payouts for a seller (useful for statement totals).
     */
    long countBySellerIdAndStatus(Long sellerId, PayoutStatus status);

    /**
     * Sum net_amount for a seller filtered by status.
     */
    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM SellerPayout p WHERE p.seller.id = :sellerId AND p.status = :status")
    java.math.BigDecimal sumNetAmountBySellerAndStatus(@Param("sellerId") Long sellerId, @Param("status") PayoutStatus status);
}
