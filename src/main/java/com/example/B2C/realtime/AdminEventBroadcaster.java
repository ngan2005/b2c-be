package com.example.B2C.realtime;

import com.example.B2C.realtime.event.OrderCreatedEvent;
import com.example.B2C.realtime.event.ReturnRequestedEvent;
import com.example.B2C.realtime.event.SellerApplicationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridges domain events to STOMP admin topics so that connected admin clients
 * receive realtime notifications.
 *
 * Publishing happens AFTER_COMMIT so a rolled-back transaction does not
 * notify the client.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminEventBroadcaster {

    private final SimpMessageSendingOperations messagingTemplate;

    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    // ── Seller applications ────────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSellerApplicationCreated(SellerApplicationCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("applicationId", event.applicationId());
        payload.put("shopName", event.shopName());
        payload.put("applicantEmail", event.applicantEmail());

        broadcast("/topic/admin/seller-applications", "SELLER_APPLICATION_CREATED",
                payload, event.occurredAt(),
                event.applicationId());
    }

    // ── Orders ─────────────────────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.orderId());
        payload.put("orderCode", event.orderCode());
        payload.put("buyerId", event.buyerId());
        payload.put("sellerId", event.sellerId());
        payload.put("totalAmount", event.totalAmount());

        broadcast("/topic/admin/orders", "ORDER_CREATED",
                payload, event.occurredAt(),
                event.orderId());
    }

    // ── Returns ────────────────────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReturnRequested(ReturnRequestedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.orderId());
        payload.put("orderCode", event.orderCode());
        payload.put("buyerId", event.buyerId());
        payload.put("sellerId", event.sellerId());
        payload.put("reason", event.reason());

        broadcast("/topic/admin/returns", "RETURN_REQUESTED",
                payload, event.occurredAt(),
                event.orderId());
    }

    // ── Shared ────────────────────────────────────────────────────────────────

    private void broadcast(String destination, String type,
                          Map<String, Object> payload,
                          Instant occurredAt,
                          Object id) {
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", type);
            envelope.put("payload", payload);
            envelope.put("occurredAt", ISO.format(occurredAt));

            // Wrap in a typed Message to resolve the convertAndSend ambiguity
            var message = MessageBuilder.withPayload(envelope).build();
            messagingTemplate.send(destination, message);
            log.info("[REALTIME:admin] sent type={} id={}", type, id);
        } catch (Exception ex) {
            log.warn("[REALTIME:admin] failed to broadcast {} id={}: {}",
                    type, id, ex.getMessage());
        }
    }

    /**
     * Public entry point used by operator tooling (e.g. the
     * {@code /api/v1/admin/realtime/test-broadcast} endpoint) to publish an
     * arbitrary event envelope without having to go through a real domain
     * transaction.
     */
    public void broadcastExternal(String destination, String type,
                                  Map<String, Object> payload,
                                  Instant occurredAt, Object id) {
        broadcast(destination, type, payload, occurredAt, id);
    }
}
