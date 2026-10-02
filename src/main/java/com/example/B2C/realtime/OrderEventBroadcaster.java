package com.example.B2C.realtime;

import com.example.B2C.common.event.OrderCancelledEvent;
import com.example.B2C.common.event.OrderCreatedEvent;
import com.example.B2C.common.event.OrderPaidEvent;
import com.example.B2C.common.event.OrderShippedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Bridges domain events to STOMP destinations so that connected clients (buyer
 * dashboards, seller admin UIs) receive realtime updates about order lifecycle
 * changes. Publishing happens AFTER_COMMIT so a rolled-back transaction does
 * not notify the client.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        broadcast(event.getOrderId(), event.getBuyerId(), event.getSellerId(),
                "ORDER_CREATED", Map.of(
                        "orderId", event.getOrderId(),
                        "status", "PENDING"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        broadcast(event.getOrderId(), event.getBuyerId(), event.getSellerId(),
                "ORDER_PAID", Map.of(
                        "orderId", event.getOrderId(),
                        "status", "CONFIRMED"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderShipped(OrderShippedEvent event) {
        broadcast(event.getOrderId(), event.getBuyerId(), event.getSellerId(),
                "ORDER_SHIPPED", Map.of(
                        "orderId", event.getOrderId(),
                        "status", "SHIPPING"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        broadcast(event.getOrderId(), event.getBuyerId(), event.getSellerId(),
                "ORDER_CANCELLED", Map.of(
                        "orderId", event.getOrderId(),
                        "status", "CANCELLED",
                        "reason", event.getReason() == null ? "" : event.getReason()));
    }

    private void broadcast(Long orderId, Long buyerId, Long sellerId, String type, Map<String, Object> payload) {
        try {
            String buyerDest = "/topic/orders/" + buyerId;
            String sellerDest = "/topic/seller/orders/" + sellerId;
            String orderDest = "/topic/order/" + orderId;
            java.util.Map<String, Object> envelope = new java.util.HashMap<>();
            envelope.put("type", type);
            envelope.put("payload", payload);
            messagingTemplate.convertAndSend(buyerDest, (Object) envelope);
            messagingTemplate.convertAndSend(sellerDest, (Object) envelope);
            messagingTemplate.convertAndSend(orderDest, (Object) envelope);
            log.debug("Broadcasted {} for order {} to buyers/sellers", type, orderId);
        } catch (Exception ex) {
            log.warn("Failed to broadcast {} for order {}: {}", type, orderId, ex.getMessage());
        }
    }
}
