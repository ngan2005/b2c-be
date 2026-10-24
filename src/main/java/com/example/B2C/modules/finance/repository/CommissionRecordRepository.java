package com.example.B2C.modules.finance.repository;

import com.example.B2C.modules.finance.entity.CommissionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, Long> {

    Optional<CommissionRecord> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

    List<CommissionRecord> findByOrderId(Long orderId);

    Page<CommissionRecord> findBySellerId(Long sellerId, Pageable pageable);

    @Query("""
        SELECT cr.order.id FROM CommissionRecord cr
        WHERE cr.order.id IN :orderIds
        """)
    List<Long> findOrderIdsWithRecords(@Param("orderIds") List<Long> orderIds);

    long countByOrderId(Long orderId);

    /**
     * Returns the sum of commission_amount for all items in a given order.
     * Returns BigDecimal.ZERO if no records exist.
     */
    @Query("SELECT COALESCE(SUM(cr.commissionAmount), 0) FROM CommissionRecord cr WHERE cr.order.id = :orderId")
    java.math.BigDecimal sumCommissionAmountByOrderId(@Param("orderId") Long orderId);
}
