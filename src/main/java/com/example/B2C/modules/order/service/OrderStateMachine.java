package com.example.B2C.modules.order.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Centralised state machine for {@link Order}. Encodes the legal transitions and
 * applies the corresponding timestamp side-effects.
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            OrderStatus.PENDING,   Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED),
            OrderStatus.PACKED,    Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING,  Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNED),
            OrderStatus.DELIVERED, Set.of(OrderStatus.COMPLETED, OrderStatus.RETURNED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of(),
            OrderStatus.RETURNED,  Set.of()
    );

    public void transition(Order order, OrderStatus target) {
        Set<OrderStatus> allowed = ALLOWED.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new BadRequestException(
                    "Cannot transition order from " + order.getStatus() + " to " + target);
        }
        order.setStatus(target);
    }

    public boolean canCancel(OrderStatus current) {
        return current == OrderStatus.PENDING || current == OrderStatus.CONFIRMED;
    }
}
