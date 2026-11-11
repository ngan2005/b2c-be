package com.example.B2C.modules.order.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends BaseRepository<Order, Long>, JpaSpecificationExecutor<Order> {

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

    default Page<Order> findByBuyerFiltered(
            Long buyerId,
            OrderStatus status,
            String search,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("buyerId"), buyerId));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                predicates.add(cb.like(cb.lower(root.get("orderCode")), pattern));
            }
            if (paymentMethod != null && !paymentMethod.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("paymentMethod")), paymentMethod.toUpperCase().trim()));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec, pageable);
    }

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

    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status = :status)
        ORDER BY o.createdAt DESC
        """)
    org.springframework.data.domain.Page<Order> findAllAdmin(
            @Param("status") OrderStatus status,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = com.example.B2C.modules.order.entity.OrderStatus.PENDING
          AND (o.paymentStatus IS NULL OR o.paymentStatus = 'UNPAID')
          AND (o.paymentMethod IS NULL OR UPPER(o.paymentMethod) <> 'COD')
          AND o.createdAt < :cutoff
        ORDER BY o.createdAt ASC
        """)
    java.util.List<Order> findExpiredUnpaidOrders(@Param("cutoff") LocalDateTime cutoff);

    // ----- Admin stats -----

    @Query("""
        SELECT FUNCTION('DATE', o.createdAt) AS day, COALESCE(SUM(o.totalAmount), 0) AS revenue
        FROM Order o
        WHERE o.createdAt >= :from
          AND o.createdAt < :to
          AND o.status NOT IN (com.example.B2C.modules.order.entity.OrderStatus.CANCELLED,
                               com.example.B2C.modules.order.entity.OrderStatus.RETURNED)
        GROUP BY FUNCTION('DATE', o.createdAt)
        ORDER BY day ASC
        """)
    java.util.List<Object[]> revenueByDayForAdmin(
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to);

    @Query("""
        SELECT FUNCTION('DATE_TRUNC', 'week', o.createdAt) AS period,
               COALESCE(SUM(o.totalAmount), 0) AS revenue
        FROM Order o
        WHERE o.createdAt >= :from
          AND o.createdAt < :to
          AND o.status NOT IN (com.example.B2C.modules.order.entity.OrderStatus.CANCELLED,
                               com.example.B2C.modules.order.entity.OrderStatus.RETURNED)
        GROUP BY FUNCTION('DATE_TRUNC', 'week', o.createdAt)
        ORDER BY period ASC
        """)
    java.util.List<Object[]> revenueByWeekForAdmin(
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to);

    @Query("""
        SELECT FUNCTION('DATE_TRUNC', 'month', o.createdAt) AS period,
               COALESCE(SUM(o.totalAmount), 0) AS revenue
        FROM Order o
        WHERE o.createdAt >= :from
          AND o.createdAt < :to
          AND o.status NOT IN (com.example.B2C.modules.order.entity.OrderStatus.CANCELLED,
                               com.example.B2C.modules.order.entity.OrderStatus.RETURNED)
        GROUP BY FUNCTION('DATE_TRUNC', 'month', o.createdAt)
        ORDER BY period ASC
        """)
    java.util.List<Object[]> revenueByMonthForAdmin(
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to);

    @Query("""
        SELECT o.status, COUNT(o)
        FROM Order o
        GROUP BY o.status
        """)
    java.util.List<Object[]> countByStatus();

    @Query(value = """
        SELECT o.sellerId, SUM(o.totalAmount) AS revenue, COUNT(o) AS cnt
        FROM Order o
        WHERE o.status IN (com.example.B2C.modules.order.entity.OrderStatus.COMPLETED,
                           com.example.B2C.modules.order.entity.OrderStatus.DELIVERED,
                           com.example.B2C.modules.order.entity.OrderStatus.SHIPPING,
                           com.example.B2C.modules.order.entity.OrderStatus.PACKED,
                           com.example.B2C.modules.order.entity.OrderStatus.CONFIRMED)
        GROUP BY o.sellerId
        ORDER BY revenue DESC
        """)
    java.util.List<Object[]> topSellersByRevenue(org.springframework.data.domain.Pageable pageable);
}