package com.example.B2C.modules.order.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends BaseRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByBuyerIdAndIdempotencyKey(Long buyerId, String idempotencyKey);

    org.springframework.data.domain.Page<Order> findByBuyerId(Long buyerId, org.springframework.data.domain.Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.buyerId = :buyerId
          AND (:status IS NULL OR o.status = :status)
        """)
    org.springframework.data.domain.Page<Order> findByBuyer(
            @Param("buyerId") Long buyerId,
            @Param("status") OrderStatus status,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.sellerId = :sellerId
          AND (:status IS NULL OR o.status = :status)
        """)
    Page<Order> findBySeller(@Param("sellerId") Long sellerId,
                             @Param("status") OrderStatus status,
                             Pageable pageable);

    Page<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    long countBySellerId(Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, OrderStatus status);

    long countByStatus(OrderStatus status);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
        WHERE o.sellerId = :sellerId
          AND o.status IN (com.example.B2C.modules.order.entity.OrderStatus.COMPLETED,
                           com.example.B2C.modules.order.entity.OrderStatus.DELIVERED,
                           com.example.B2C.modules.order.entity.OrderStatus.SHIPPING,
                           com.example.B2C.modules.order.entity.OrderStatus.PACKED,
                           com.example.B2C.modules.order.entity.OrderStatus.CONFIRMED)
        """)
    java.math.BigDecimal sumRevenueBySeller(@Param("sellerId") Long sellerId);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
        WHERE o.status IN (com.example.B2C.modules.order.entity.OrderStatus.COMPLETED,
                           com.example.B2C.modules.order.entity.OrderStatus.DELIVERED,
                           com.example.B2C.modules.order.entity.OrderStatus.SHIPPING,
                           com.example.B2C.modules.order.entity.OrderStatus.PACKED,
                           com.example.B2C.modules.order.entity.OrderStatus.CONFIRMED)
        """)
    java.math.BigDecimal sumRevenueAll();

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
        WHERE o.sellerId = :sellerId
          AND o.status IN (com.example.B2C.modules.order.entity.OrderStatus.COMPLETED,
                           com.example.B2C.modules.order.entity.OrderStatus.DELIVERED,
                           com.example.B2C.modules.order.entity.OrderStatus.SHIPPING,
                           com.example.B2C.modules.order.entity.OrderStatus.PACKED,
                           com.example.B2C.modules.order.entity.OrderStatus.CONFIRMED)
          AND o.createdAt >= :since
        """)
    java.math.BigDecimal sumRevenueBySellerSince(@Param("sellerId") Long sellerId,
                                                  @Param("since") LocalDateTime since);

    @Query("""
        SELECT FUNCTION('DATE', o.createdAt) AS day, COALESCE(SUM(o.totalAmount), 0) AS revenue
        FROM Order o
        WHERE o.sellerId = :sellerId
          AND o.createdAt >= :since
          AND o.status <> com.example.B2C.modules.order.entity.OrderStatus.CANCELLED
          AND o.status <> com.example.B2C.modules.order.entity.OrderStatus.RETURNED
        GROUP BY FUNCTION('DATE', o.createdAt)
        ORDER BY day ASC
        """)
    java.util.List<Object[]> revenueByDayForSeller(@Param("sellerId") Long sellerId,
                                                   @Param("since") LocalDateTime since);
}