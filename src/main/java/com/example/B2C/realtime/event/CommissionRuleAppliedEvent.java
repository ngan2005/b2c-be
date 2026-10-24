package com.example.B2C.realtime.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published after a commission-rule mutation (create / update / disable) commits.
 * The {@link com.example.B2C.realtime.SellerEventBroadcaster} listens for this
 * event and pushes it to /topic/sellers/commission-rules.
 *
 * @param notificationId the newly inserted seller_notification.id
 * @param ruleId         the commission rule id
 * @param ruleName       the commission rule name
 * @param percent        commission rate as a percentage
 * @param action         one of CREATED | UPDATED | DISABLED
 * @param occurredAt     ISO-8601 instant
 */
public record CommissionRuleAppliedEvent(
        Long notificationId,
        Long ruleId,
        String ruleName,
        BigDecimal percent,
        String action,
        Instant occurredAt
) {
}
