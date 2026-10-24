package com.example.B2C.modules.admin.controller;

import com.example.B2C.realtime.SseEmitterRegistry;
import com.example.B2C.realtime.SseOrderEventBridge;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Synthetic event publisher for the SSE admin pipeline. Used by manual
 * browser tests to confirm an {@code EventSource} client connected to
 * {@code /api/v1/admin/sse/orders} or {@code .../returns} receives the push.
 *
 * <p>Uses the same {@code ?token=} auth flow as the streaming endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/sse")
@RequiredArgsConstructor
public class AdminSseTestController {

    private final SseEmitterRegistry registry;
    private final SseAdminAuth auth;

    @PostMapping("/test-broadcast")
    public Map<String, Object> testBroadcast(@RequestParam("token") String token) {
        auth.requireAdmin(token);
        long nonce = ThreadLocalRandom.current().nextLong(1000, 9999);
        Instant now = Instant.now();

        // 1) Synthetic ORDER_CREATED on the orders stream
        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("orderId", nonce + 1000);
        orderPayload.put("orderCode", "SSE-TEST-" + nonce);
        orderPayload.put("buyerId", 1L);
        orderPayload.put("sellerId", 2L);
        orderPayload.put("totalAmount", 250_000);
        orderPayload.put("status", "PENDING");
        Map<String, Object> orderEnvelope = new HashMap<>();
        orderEnvelope.put("type", "ORDER_CREATED");
        orderEnvelope.put("payload", orderPayload);
        orderEnvelope.put("occurredAt", now.toString());
        registry.publish(SseOrderEventBridge.TOPIC_ORDERS, orderEnvelope);

        // 2) Synthetic RETURN_REQUESTED on the returns stream
        Map<String, Object> returnPayload = new HashMap<>();
        returnPayload.put("orderId", nonce + 2000);
        returnPayload.put("orderCode", "SSE-RET-" + nonce);
        returnPayload.put("buyerId", 1L);
        returnPayload.put("sellerId", 2L);
        returnPayload.put("reason", "Defective unit (synthetic)");
        Map<String, Object> returnEnvelope = new HashMap<>();
        returnEnvelope.put("type", "RETURN_REQUESTED");
        returnEnvelope.put("payload", returnPayload);
        returnEnvelope.put("occurredAt", now.toString());
        registry.publish(SseOrderEventBridge.TOPIC_RETURNS, returnEnvelope);

        return Map.of(
                "ordersListeners", registry.connectionCount(SseOrderEventBridge.TOPIC_ORDERS),
                "returnsListeners", registry.connectionCount(SseOrderEventBridge.TOPIC_RETURNS),
                "nonce", nonce);
    }
}
