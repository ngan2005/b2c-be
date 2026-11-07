package com.example.B2C.modules.order.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.order.entity.OrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends BaseRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
        SELECT COALESCE(SUM(oi.quantity), 0)
        FROM OrderItem oi, Order o
        WHERE oi.orderId = o.id
          AND oi.variantId = :variantId
          AND o.status IN (com.example.B2C.modules.order.entity.OrderStatus.PENDING,
                           com.example.B2C.modules.order.entity.OrderStatus.CONFIRMED,
                           com.example.B2C.modules.order.entity.OrderStatus.PACKED,
                           com.example.B2C.modules.order.entity.OrderStatus.SHIPPING)
          AND (o.paymentStatus IS NULL OR o.paymentStatus <> 'PAID')
        """)
    int sumActiveReservationsByVariantId(@Param("variantId") Long variantId);
}

