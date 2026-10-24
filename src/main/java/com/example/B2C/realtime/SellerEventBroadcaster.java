package com.example.B2C.realtime;

import com.example.B2C.realtime.event.CommissionRuleAppliedEvent;
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
 * Bridges {@link CommissionRuleAppliedEvent} (published after a commission-rule
 * mutation commits) to the STOMP topic {@code /topic/sellers/commission-rules}
 * so that connected seller clients receive the notification in real time.
 *
 * Publishing happens AFTER_COMMIT so a rolled-back transaction does not
 * notify the client.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SellerEventBroadcaster {

    private final SimpMessageSendingOperations messagingTemplate;

    private static final String DESTINATION = "/topic/sellers/commission-rules";
    private static final String TYPE        = "COMMISSION_RULE_APPLIED";
    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommissionRuleApplied(CommissionRuleAppliedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", event.notificationId());
        payload.put("ruleId",        event.ruleId());
        payload.put("ruleName",      event.ruleName());
        payload.put("rulePercent",   event.percent());
        payload.put("action",        event.action());
        payload.put("occurredAt",    ISO.format(event.occurredAt()));

        broadcast(payload, event.occurredAt(), event.notificationId());
    }

    private void broadcast(Map<String, Object> payload, Instant occurredAt, Object id) {
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type",       TYPE);
            envelope.put("payload",   payload);
            envelope.put("occurredAt", ISO.format(occurredAt));

            var message = MessageBuilder.withPayload(envelope).build();
            messagingTemplate.send(DESTINATION, message);
            log.info("[REALTIME:seller] sent type={} id={}", TYPE, id);
        } catch (Exception ex) {
            log.warn("[REALTIME:seller] failed to broadcast {} id={}: {}",
                    TYPE, id, ex.getMessage());
        }
    }
}
