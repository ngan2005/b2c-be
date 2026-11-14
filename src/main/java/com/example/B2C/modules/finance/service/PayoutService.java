package com.example.B2C.modules.finance.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.BusinessException;
import com.example.B2C.common.exception.ErrorCode;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.finance.dto.SellerPayoutDto;
import com.example.B2C.modules.finance.dto.SellerStatementDto;
import com.example.B2C.modules.finance.entity.PayoutStatus;
import com.example.B2C.modules.finance.entity.SellerPayout;
import com.example.B2C.modules.finance.repository.SellerPayoutRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing seller payouts and the payout ledger.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutService {

    private static final int RECENT_PAYOUTS_LIMIT = 50;

    private final SellerPayoutRepository payoutRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;
    private final PlatformWalletService platformWalletService;

    // ---------------- reads ----------------

    /**
     * Lists all payouts, optionally filtered by status, with pagination.
     */
    @Transactional(readOnly = true)
    public Page<SellerPayoutDto> listPayouts(PayoutStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SellerPayout> result = (status == null)
                ? payoutRepository.findAll(pageable)
                : payoutRepository.findByStatus(status, pageable);
        return result.map(this::toEnrichedDto);
    }

    /**
     * Lists payouts belonging specifically to a seller, optionally filtered by
     * status.
     */
    @Transactional(readOnly = true)
    public Page<SellerPayoutDto> listPayoutsForSeller(Long sellerId, PayoutStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SellerPayout> result = payoutRepository.findBySellerIdAndOptionalStatus(sellerId, status, pageable);
        return result.map(this::toEnrichedDto);
    }

    /**
     * Returns a single payout by id, or throws.
     */
    @Transactional(readOnly = true)
    public SellerPayoutDto getPayout(Long id) {
        return payoutRepository.findById(id)
                .map(this::toEnrichedDto)
                .orElseThrow(() -> new ResourceNotFoundException("SellerPayout", "id: " + id));
    }

    /**
     * Returns a seller's statement: totals and recent payout history.
     */
    @Transactional(readOnly = true)
    public SellerStatementDto getSellerStatement(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + sellerId));

        BigDecimal totalPending = payoutRepository.sumNetAmountBySellerAndStatus(sellerId, PayoutStatus.PENDING);
        BigDecimal totalReady = payoutRepository.sumNetAmountBySellerAndStatus(sellerId, PayoutStatus.READY);
        BigDecimal totalPaid = payoutRepository.sumNetAmountBySellerAndStatus(sellerId, PayoutStatus.PAID);
        BigDecimal safePending = totalPending != null ? totalPending : BigDecimal.ZERO;
        BigDecimal safeReady = totalReady != null ? totalReady : BigDecimal.ZERO;
        BigDecimal safePaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
        BigDecimal lifetime = safePending.add(safeReady).add(safePaid);

        List<SellerPayoutDto> recentPayouts = payoutRepository
                .findBySellerIdAndStatus(sellerId, PayoutStatus.PAID)
                .stream()
                .map(this::toEnrichedDto)
                .toList();

        return SellerStatementDto.builder()
                .totalPending(safePending)
                .totalReady(safeReady)
                .totalPaid(safePaid)
                .lifetimeEarned(lifetime)
                .shopName(seller.getShopName())
                .recentPayouts(recentPayouts)
                .build();
    }

    private SellerPayoutDto toEnrichedDto(SellerPayout p) {
        SellerPayoutDto dto = SellerPayoutDto.from(p);
        if (p.getOrderId() != null && dto.getOrderCode() == null) {
            orderRepository.findById(p.getOrderId()).ifPresent(o -> dto.setOrderCode(o.getOrderCode()));
        }
        return dto;
    }

    /**
     * Allows a seller to submit a payout request, transitioning eligible PENDING
     * payouts to READY.
     *
     * @param sellerId seller requesting payout
     * @return count of payouts moved to READY
     */
    @Transactional
    public int requestPayoutForSeller(Long sellerId) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("Seller", "id: " + sellerId);
        }

        List<SellerPayout> pendingPayouts = payoutRepository.findBySellerIdAndStatus(sellerId, PayoutStatus.PENDING);
        if (pendingPayouts.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (SellerPayout payout : pendingPayouts) {
            payout.setStatus(PayoutStatus.READY);
            payoutRepository.save(payout);
            count++;
        }
        log.info("Seller {} requested payout: moved {} pending payout records to READY", sellerId, count);
        return count;
    }

    // ---------------- writes ----------------

    /**
     * Creates a PENDING payout record for a completed order.
     *
     * <p>
     * Idempotent: if a non-cancelled payout already exists for this order,
     * the method returns null without creating a duplicate.
     *
     * @param order            the completed order
     * @param commissionAmount platform commission for this order (from workstream
     *                         A)
     * @return the created payout, or null if idempotently skipped
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SellerPayout createForCompletedOrder(Order order, BigDecimal commissionAmount) {
        if (payoutRepository.existsByOrderIdAndStatusNot(order.getId(), PayoutStatus.CANCELLED)) {
            log.debug("Payout already exists for order {}; skipping", order.getId());
            return null;
        }

        if (order.getSellerId() == null) {
            log.warn("Order {} has no sellerId, skipping payout creation", order.getId());
            return null;
        }

        Seller seller = sellerRepository.findById(order.getSellerId()).orElse(null);
        if (seller == null) {
            log.warn("Seller {} not found for order {}, skipping payout creation", order.getSellerId(), order.getId());
            return null;
        }

        BigDecimal gross = order.getTotalAmount();
        BigDecimal net = gross.subtract(commissionAmount);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            net = BigDecimal.ZERO;
            log.warn("Order {} net payout would be negative; clamping to 0 (gross={}, commission={})",
                    order.getId(), gross, commissionAmount);
        }

        SellerPayout payout = SellerPayout.builder()
                .seller(seller)
                .orderId(order.getId())
                .grossAmount(gross)
                .commissionAmount(commissionAmount)
                .netAmount(net)
                .status(PayoutStatus.PENDING)
                .build();

        SellerPayout saved = payoutRepository.save(payout);
        log.info("Created PENDING payout {} for order {}: gross={} commission={} net={}",
                saved.getId(), order.getId(), gross, commissionAmount, net);
        return saved;
    }

    /**
     * Marks a payout as PAID and records the bank reference.
     *
     * <p>
     * Debits the platform wallet by the net amount and writes a transaction row.
     * Only callable when the payout is in PENDING or READY status.
     *
     * @param payoutId      payout record id
     * @param adminUserId   admin user performing the action
     * @param bankReference bank transfer reference
     * @return the updated payout DTO
     */
    @Transactional
    public SellerPayoutDto markPaid(Long payoutId, Long adminUserId, String bankReference) {
        SellerPayout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerPayout", "id: " + payoutId));

        if (payout.getStatus() == PayoutStatus.PAID) {
            throw new BusinessException(ErrorCode.PAYOUT_ALREADY_PAID);
        }
        if (payout.getStatus() == PayoutStatus.CANCELLED) {
            throw new BadRequestException("Cannot mark a CANCELLED payout as PAID");
        }

        payout.setStatus(PayoutStatus.PAID);
        payout.setBankReference(bankReference);
        payout.setPaidAt(LocalDateTime.now());
        payout.setPaidBy(adminUserId);

        SellerPayout saved = payoutRepository.save(payout);

        // Debit platform wallet (net amount = what the seller earned; the commission
        // was already credited when the order completed, so this DEBIT reverses that
        // portion)
        Long sellerId = saved.getSeller() != null ? saved.getSeller().getId() : null;
        String note = "Payout " + saved.getId() + " to seller " + sellerId
                + (bankReference != null ? " ref:" + bankReference : "");
        try {
            platformWalletService.debit(
                    saved.getNetAmount(),
                    "PAYOUT",
                    saved.getOrderId(),
                    sellerId,
                    note);
        } catch (IllegalStateException ex) {
            log.error("Wallet debit failed for payout {}: {}. Manual reconciliation required.",
                    payoutId, ex.getMessage());
            throw new BadRequestException("Platform wallet debit failed: " + ex.getMessage()
                    + ". Please reconcile manually before marking this payout as paid.");
        }

        log.info("Marked payout {} as PAID (admin={}, bankRef={})",
                payoutId, adminUserId, bankReference);
        return SellerPayoutDto.from(saved);
    }

    /**
     * Cancels a PENDING payout. Cannot cancel PAID or already CANCELLED payouts.
     */
    @Transactional
    public SellerPayoutDto cancelPayout(Long payoutId) {
        SellerPayout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerPayout", "id: " + payoutId));

        if (payout.getStatus() == PayoutStatus.PAID) {
            throw new BadRequestException("Cannot cancel a PAID payout");
        }
        if (payout.getStatus() == PayoutStatus.CANCELLED) {
            throw new BadRequestException("Payout is already CANCELLED");
        }

        payout.setStatus(PayoutStatus.CANCELLED);
        SellerPayout saved = payoutRepository.save(payout);
        log.info("Cancelled payout {} (was status={})", payoutId, payout.getStatus());
        return SellerPayoutDto.from(saved);
    }
}
