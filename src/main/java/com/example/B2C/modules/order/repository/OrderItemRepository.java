package com.example.B2C.modules.order.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.order.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository extends BaseRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
}
