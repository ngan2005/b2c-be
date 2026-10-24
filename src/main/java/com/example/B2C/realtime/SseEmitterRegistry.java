package com.example.B2C.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory registry of SseEmitters keyed by topic name.
 *
 * <p>The admin SPA opens a long-lived HTTP connection to
 * {@code /api/v1/admin/sse/orders} (and {@code .../returns}). Each connected
 * client gets its own {@link SseEmitter} which we keep here so that
 * {@link com.example.B2C.realtime.SseOrderEventBridge} can fan-out domain
 * events to all admins currently watching the dashboard.
 *
 * <p>Emitters are removed automatically on completion/timeout/error, and
 * failures to write to a specific emitter are swallowed so that one slow or
 * broken client cannot block delivery to the rest.
 */
@Component
@Slf4j
public class SseEmitterRegistry {

    /** topic -> list of currently connected emitters */
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(String topic, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);

        emitters.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> {
            List<SseEmitter> list = emitters.get(topic);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) {
                    emitters.remove(topic);
                }
            }
            log.debug("[SSE] Removed emitter for topic={} (remaining={})",
                    topic, list == null ? 0 : list.size());
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Send a hello event so the client knows the connection is alive
        try {
            emitter.send(SseEmitter.event()
                    .name("hello")
                    .data(Map.of("topic", topic, "ts", System.currentTimeMillis())));
        } catch (IOException e) {
            cleanup.run();
        }

        log.info("[SSE] Registered emitter for topic={} (total={})", topic,
                emitters.get(topic) == null ? 0 : emitters.get(topic).size());
        return emitter;
    }

    /**
     * Publish a JSON payload to every emitter subscribed to {@code topic}.
     * Failures are caught and the offending emitter is removed so it does not
     * block subsequent deliveries.
     */
    public void publish(String topic, Object payload) {
        List<SseEmitter> list = emitters.get(topic);
        if (list == null || list.isEmpty()) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(payload, org.springframework.http.MediaType.APPLICATION_JSON));
            } catch (Exception ex) {
                log.warn("[SSE] Failed to deliver to emitter topic={}, dropping: {}",
                        topic, ex.getMessage());
                try {
                    emitter.completeWithError(ex);
                } catch (Exception ignored) {
                    // already complete
                }
            }
        }
    }

    public int connectionCount(String topic) {
        List<SseEmitter> list = emitters.get(topic);
        return list == null ? 0 : list.size();
    }
}
