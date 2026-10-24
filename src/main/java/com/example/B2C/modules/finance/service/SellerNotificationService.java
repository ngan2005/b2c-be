package com.example.B2C.modules.finance.service;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.finance.dto.SellerNotificationDto;
import com.example.B2C.modules.finance.entity.CommissionRule;
import com.example.B2C.modules.finance.entity.SellerNotification;
import com.example.B2C.modules.finance.repository.SellerNotificationRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.realtime.event.CommissionRuleAppliedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SellerNotificationService {

    private final SellerNotificationRepository notificationRepository;
    private final SellerRepository sellerRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String TYPE = "COMMISSION_RULE_APPLIED";

    /**
     * Resolves a {@code Seller.id} from an authenticated {@code User.id}.
     * The security principal exposed by {@code CurrentUserProvider} is the
     * {@code User}, but the {@link SellerNotification} foreign key points to
     * the {@code seller} table — so we must bridge the two before reading or
     * writing notifications on behalf of a seller.
     *
     * @throws com.example.B2C.common.exception.ResourceNotFoundException if the
     *         authenticated user does not own a seller profile.
     */
    private Long resolveSellerId(Long userId) {
        Long sellerId = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[SellerNotification] No Seller profile for userId={}", userId);
                    return new ResourceNotFoundException(
                            "Seller", "userId: " + userId);
                })
                .getId();
        log.debug("[SellerNotification] Resolved userId={} -> sellerId={}", userId, sellerId);
        return sellerId;
    }

    /**
     * Creates a seller_notification row for every active seller and publishes
     * a CommissionRuleAppliedEvent after the current transaction commits.
     * Must be called from within a {@code @Transactional} method so that the
     * AFTER_COMMIT listener picks it up.
     */
    @Transactional
    public void notifyAllSellers(CommissionRule rule, String action) {
        List<Seller> sellers = sellerRepository.findAll();

        String title = switch (action) {
            case "CREATED"  -> "📜 Sàn vừa áp dụng quy định phí mới — " + rule.getName();
            case "UPDATED"  -> "✏️ Sàn vừa cập nhật quy định phí — " + rule.getName();
            case "DISABLED" -> "🚫 Sàn vừa vô hiệu hóa quy định phí — " + rule.getName();
            default         -> "🔔 Thông báo từ sàn — " + rule.getName();
        };

        String body = buildBody(rule, action);

        // Save all notification rows and capture their ids while still in the transaction
        List<Long> notificationIds = sellers.stream().map(seller -> {
            SellerNotification notif = SellerNotification.builder()
                    .seller(seller)
                    .type(TYPE)
                    .title(title)
                    .body(body)
                    .ruleId(rule.getId())
                    .ruleName(rule.getName())
                    .rulePercent(rule.getPercent())
                    .build();
            return notificationRepository.save(notif).getId();
        }).toList();

        log.info("[SellerNotification] {} sellers notified for rule {} action={}",
                sellers.size(), rule.getId(), action);

        // Publish AFTER the transaction commits so the STOMP broadcast
        // happens only when the DB write is guaranteed.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // Publish one event per notification id so the FE can
                        // push each into the seller's notification store.
                        for (Long notifId : notificationIds) {
                            eventPublisher.publishEvent(new CommissionRuleAppliedEvent(
                                    notifId,
                                    rule.getId(),
                                    rule.getName(),
                                    rule.getPercent(),
                                    action,
                                    Instant.now()
                            ));
                        }
                    }
                }
        );
    }

    public PageResponse<SellerNotificationDto> listForSeller(Long userId, int page, int size) {
        Long sellerId = resolveSellerId(userId);
        log.info("[SellerNotification] list: userId={} sellerId={} page={} size={}", userId, sellerId, page, size);
        int s = Math.min(Math.max(size, 1), 100);
        Page<SellerNotificationDto> p = notificationRepository
                .findBySellerIdOrderByCreatedAtDesc(sellerId, PageRequest.of(page, s))
                .map(SellerNotificationDto::from);
        log.info("[SellerNotification] list result: totalElements={} returned={}",
                p.getTotalElements(), p.getNumberOfElements());
        return PageResponse.of(p);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Long sellerId = resolveSellerId(userId);
        Optional<SellerNotification> notif =
                notificationRepository.findByIdAndSellerId(notificationId, sellerId);
        notif.ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        Long sellerId = resolveSellerId(userId);
        notificationRepository.findBySellerIdOrderByCreatedAtDesc(sellerId,
                        PageRequest.of(0, 1000))
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationRepository.save(n);
                });
    }

    public long getUnreadCount(Long userId) {
        Long sellerId = resolveSellerId(userId);
        return notificationRepository.countBySellerIdAndIsReadFalse(sellerId);
    }

    private String buildBody(CommissionRule rule, String action) {
        String percent = rule.getPercent() + "%";
        String scope = rule.getCategory() != null
                ? "danh mục [" + rule.getCategory().getName() + "]"
                : "toàn sàn";
        String effective = rule.getEffectiveFrom() != null
                ? LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "hiện tại";

        return "Phí sàn là % sàn thu trên mỗi đơn hàng hoàn thành. " +
                "Mức phí mới sẽ áp dụng cho các đơn phát sinh từ " + effective + ". " +
                "Phí: " + percent + " | Phạm vi: " + scope + ".";
    }
}
