package com.example.B2C.realtime;

import com.example.B2C.common.event.OrderCancelledEvent;
import com.example.B2C.common.event.OrderPaidEvent;
import com.example.B2C.common.event.OrderShippedEvent;
import com.example.B2C.realtime.event.OrderCreatedEvent;
import com.example.B2C.realtime.event.ReturnRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridges domain events to the in-memory SSE emitter registry so that admin
 * browsers subscribed via {@code /api/v1/admin/sse/orders} (and
 * {@code .../returns}) receive realtime updates about order lifecycle changes.
 *
 * <p>Publishing happens AFTER_COMMIT so a rolled-back transaction does not
 * notify the client. The bridge runs synchronously; the underlying
 * {@link SseEmitterRegistry} catches per-client write failures and drops
 * broken emitters, so a slow subscriber cannot block the producer thread for
 * long.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SseOrderEventBridge {

    /** Admin orders topic — also serves as the SSE emitter key. */
    public static final String TOPIC_ORDERS = "admin/orders";
    /** Admin returns topic. */
    public static final String TOPIC_RETURNS = "admin/returns";

    private final SseEmitterRegistry registry;

    // ── Orders ─────────────────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.orderId());
        payload.put("orderCode", event.orderCode());
        payload.put("buyerId", event.buyerId());
        payload.put("sellerId", event.sellerId());
        payload.put("totalAmount", event.totalAmount());
        payload.put("status", "PENDING");
        publish(TOPIC_ORDERS, "ORDER_CREATED", payload, event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("buyerId", event.getBuyerId());
        payload.put("sellerId", event.getSellerId());
        payload.put("status", "CONFIRMED");
        publish(TOPIC_ORDERS, "ORDER_PAID", payload, Instant.now());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderShipped(OrderShippedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("buyerId", event.getBuyerId());
        payload.put("sellerId", event.getSellerId());
        payload.put("status", "SHIPPING");
        publish(TOPIC_ORDERS, "ORDER_SHIPPED", payload, Instant.now());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("buyerId", event.getBuyerId());
        payload.put("sellerId", event.getSellerId());
        payload.put("status", "CANCELLED");
        payload.put("reason", event.getReason() == null ? "" : event.getReason());
        publish(TOPIC_ORDERS, "ORDER_CANCELLED", payload, Instant.now());
    }

    // ── Returns ────────────────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReturnRequested(ReturnRequestedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.orderId());
        payload.put("orderCode", event.orderCode());
        payload.put("buyerId", event.buyerId());
        payload.put("sellerId", event.sellerId());
        payload.put("reason", event.reason() == null ? "" : event.reason());
        publish(TOPIC_RETURNS, "RETURN_REQUESTED", payload, event.occurredAt());
    }

    // ── Shared ─────────────────────────────────────────────────────────────

    private void publish(String topic, String type, Map<String, Object> payload, Instant occurredAt) {
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", type);
            envelope.put("payload", payload);
            envelope.put("occurredAt", occurredAt == null ? Instant.now().toString() : occurredAt.toString());
            registry.publish(topic, envelope);
            log.info("[SSE:{}] sent type={} orderId={}", topic, type, payload.get("orderId"));
        } catch (Exception ex) {
            log.warn("[SSE:{}] failed to broadcast {} orderId={}: {}",
                    topic, type, payload.get("orderId"), ex.getMessage());
        }
    }
}
