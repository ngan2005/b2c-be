package com.example.B2C.modules.admin.controller;

import com.example.B2C.realtime.SseEmitterRegistry;
import com.example.B2C.realtime.SseOrderEventBridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-Sent Events endpoints for the admin SPA.
 *
 * <p>The admin Orders dashboard subscribes here for real-time push of order
 * lifecycle changes (Created / Paid / Shipped / Cancelled) and return
 * requests. EventSource cannot send custom {@code Authorization} headers, so
 * the JWT is passed as a {@code ?token=} query parameter and validated
 * manually by {@link SseAdminAuth}. Only callers whose token belongs to a
 * user with the {@code ROLE_ADMIN} authority may open a stream.
 */
@RestController
@RequestMapping("/api/v1/admin/sse")
@RequiredArgsConstructor
@Slf4j
public class AdminSseController {

    /** 30 minutes — long enough for a working session, short enough to refresh tokens. */
    private static final long EMITTER_TIMEOUT_MS = 30L * 60L * 1000L;

    private final SseEmitterRegistry registry;
    private final SseAdminAuth auth;

    @GetMapping(value = "/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrders(@RequestParam(value = "token", required = false) String token) {
        auth.requireAdmin(token);
        SseEmitter emitter = registry.register(SseOrderEventBridge.TOPIC_ORDERS, EMITTER_TIMEOUT_MS);
        log.info("[SSE] admin opened orders stream (total={})",
                registry.connectionCount(SseOrderEventBridge.TOPIC_ORDERS));
        return emitter;
    }

    @GetMapping(value = "/returns", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReturns(@RequestParam(value = "token", required = false) String token) {
        auth.requireAdmin(token);
        SseEmitter emitter = registry.register(SseOrderEventBridge.TOPIC_RETURNS, EMITTER_TIMEOUT_MS);
        log.info("[SSE] admin opened returns stream (total={})",
                registry.connectionCount(SseOrderEventBridge.TOPIC_RETURNS));
        return emitter;
    }
}
