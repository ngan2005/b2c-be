package com.example.B2C.modules.admin.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.realtime.AdminEventBroadcaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/v1/admin/realtime")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Realtime", description = "Synthetic event broadcast for testing STOMP subscriptions")
public class AdminRealtimeController {

    private final AdminEventBroadcaster broadcaster;

    @PostMapping("/test-broadcast")
    @Operation(summary = "Publish one synthetic event on each admin topic so operators can verify the WebSocket pipeline")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testBroadcast() {
        long nonce = ThreadLocalRandom.current().nextLong(1000, 9999);
        Instant now = Instant.now();

        broadcaster.broadcastExternal(
                "/topic/admin/seller-applications",
                "SELLER_APPLICATION_CREATED",
                Map.of(
                        "applicationId", nonce,
                        "shopName", "Test Shop #" + nonce,
                        "applicantEmail", "test+" + nonce + "@b2c.local"),
                now,
                nonce);

        broadcaster.broadcastExternal(
                "/topic/admin/orders",
                "ORDER_CREATED",
                Map.of(
                        "orderId", nonce + 1000,
                        "orderCode", "TEST-" + nonce,
                        "buyerId", 1L,
                        "sellerId", 2L,
                        "totalAmount", 100_000.0),
                now,
                nonce + 1000);

        broadcaster.broadcastExternal(
                "/topic/admin/returns",
                "RETURN_REQUESTED",
                Map.of(
                        "orderId", nonce + 1000,
                        "orderCode", "TEST-" + nonce,
                        "buyerId", 1L,
                        "sellerId", 2L,
                        "reason", "QA test broadcast"),
                now,
                nonce + 1000);

        broadcaster.broadcastExternal(
                "/topic/admin/disputes",
                "DISPUTE_OPENED",
                Map.of(
                        "orderId", nonce + 2000,
                        "orderCode", "TEST-D-" + nonce,
                        "buyerId", 1L,
                        "disputeType", "NOT_RECEIVED",
                        "description", "QA test dispute broadcast"),
                now,
                nonce + 2000);

        String[] topics = new String[]{
                "/topic/admin/seller-applications",
                "/topic/admin/orders",
                "/topic/admin/returns",
                "/topic/admin/disputes"
        };

        return ResponseEntity.ok(ApiResponse.success(
                "Broadcast 4 test events to /topic/admin/*",
                Map.of("nonce", nonce, "topics", topics)));
    }
}
