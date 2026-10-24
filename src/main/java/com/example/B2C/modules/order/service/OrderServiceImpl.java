package com.example.B2C.modules.order.service;

import com.example.B2C.common.event.OrderCancelledEvent;
import com.example.B2C.common.event.OrderCreatedEvent;
import com.example.B2C.common.event.OrderShippedEvent;
import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.BusinessException;
import com.example.B2C.common.exception.ErrorCode;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.idempotency.IdempotencyService;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.cart.entity.Cart;
import com.example.B2C.modules.cart.entity.CartItem;
import com.example.B2C.modules.cart.repository.CartRepository;
import com.example.B2C.modules.cart.service.CartService;
import com.example.B2C.modules.catalog.api.CatalogFacade;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderItemDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.dto.ReturnRequestDto;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.payment.entity.Payment;
import com.example.B2C.modules.payment.entity.PaymentTransactionStatus;
import com.example.B2C.modules.payment.repository.PaymentRepository;
import com.example.B2C.modules.payment.config.VnPayConfig;
import com.example.B2C.modules.payment.service.VnPayService;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Address;
import com.example.B2C.modules.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;
    private final CartRepository cartRepository;
    private final com.example.B2C.modules.cart.repository.CartItemRepository cartItemRepository;
    private final CatalogFacade catalogFacade;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryPort inventoryPort;
    private final IdempotencyService idempotencyService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderStateMachine stateMachine;
    private final VnPayService vnpayService;
    private final VnPayConfig vnpayConfig;
    private final com.example.B2C.modules.finance.service.CommissionService commissionService;
    private final com.example.B2C.modules.finance.service.PayoutService payoutService;
    private final com.example.B2C.modules.finance.service.PlatformWalletService platformWalletService;
    private final com.example.B2C.modules.finance.repository.CommissionRecordRepository commissionRecordRepository;
    private final com.example.B2C.modules.promotion.repository.PromotionRepository promotionRepository;
    private final com.example.B2C.modules.promotion.service.PromotionService promotionService;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING,   Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED),
            OrderStatus.PACKED,    Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING,   Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(OrderStatus.COMPLETED, OrderStatus.RETURN_REQUESTED),
            OrderStatus.RETURN_REQUESTED, Set.of(OrderStatus.RETURNED, OrderStatus.DELIVERED),
            OrderStatus.COMPLETED,  Set.of(),
            OrderStatus.CANCELLED,  Set.of(),
            OrderStatus.RETURNED,   Set.of()
    );

    // ---------------- Seller side (unchanged) ----------------

    @Override
    public PageResponse<OrderSummaryDto> listOrdersForCurrentSeller(Long userId, OrderStatus status, int page, int size) {
        Long sellerId = getActiveSellerId(userId);
        int p = Math.max(page, 0);
        int s = clampSize(size);
        var pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findBySeller(sellerId, status, pageable);
        return PageResponse.of(orders.map(OrderSummaryDto::from));
    }

    @Override
    public OrderDetailDto getOrderForSeller(Long userId, Long orderId) {
        Long sellerId = getActiveSellerId(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!sellerId.equals(order.getSellerId())) {
            throw new ForbiddenException("You do not own this order");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toDetailDto(order, items);
    }

    @Override
    @Transactional
    public OrderDetailDto updateOrderStatusForSeller(Long userId, Long orderId, OrderStatus newStatus, String note) {
        Long sellerId = getActiveSellerId(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!sellerId.equals(order.getSellerId())) {
            throw new ForbiddenException("You do not own this order");
        }

        // Idempotent cancel: if the seller re-sends CANCELLED for an already
        // cancelled order, return the current state without releasing stock twice
        // and without re-publishing the cancelled event.
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() == OrderStatus.CANCELLED) {
            List<OrderItem> existingItems = orderItemRepository.findByOrderId(orderId);
            return toDetailDto(order, existingItems);
        }

        stateMachine.transition(order, newStatus);
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(now);
            case SHIPPING -> order.setShippedAt(now);
            case DELIVERED -> order.setDeliveredAt(now);
            case COMPLETED -> order.setCompletedAt(now);
            case CANCELLED -> {
                order.setCancelledAt(now);
                order.setCancelReason(note);
                order.setCancelledBy(userId);
            }
            case RETURN_REQUESTED -> order.setReturnRequestedAt(now);
            case RETURNED -> {
                order.setReturnApprovedAt(now);
                if (note != null) order.setReturnAdminNote(note);
            }
            default -> {}
        }

        if (note != null && !note.isBlank() && newStatus != OrderStatus.CANCELLED) {
            order.setNote(note);
        }
        Order saved = orderRepository.save(order);
        log.info("Seller {} moved order {} to status {}", sellerId, orderId, newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            releaseStockForOrder(orderId);
            eventPublisher.publishEvent(new OrderCancelledEvent(this, orderId, order.getBuyerId(), sellerId, note));
        }
        if (newStatus == OrderStatus.SHIPPING) {
            eventPublisher.publishEvent(new OrderShippedEvent(this, orderId, order.getBuyerId(), sellerId));
        }

        // ── Commission snapshot on COMPLETED ──
        // Order completion is the source of truth; commission records are derived.
        // Failures here must NOT break order completion — log and continue.
        if (newStatus == OrderStatus.COMPLETED) {
            List<OrderItem> itemsForCommission = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : itemsForCommission) {
                try {
                    // CommissionService.snapshotForItem is idempotent (skips if record already exists)
                    // and guarded by a unique constraint on order_item_id.
                    commissionService.snapshotForItem(item, saved);
                } catch (Exception ex) {
                    log.warn("Failed to snapshot commission for order_item_id={} on order {}: {}",
                            item.getId(), orderId, ex.getMessage());
                }
            }

            // ── Payout record + platform wallet credit on COMPLETED ──
            // Wrapped in try/catch so that order completion is never blocked.
            try {
                // Sum total commission across all items in this order.
                java.math.BigDecimal commissionTotal = commissionRecordRepository
                        .sumCommissionAmountByOrderId(saved.getId());
                java.math.BigDecimal safeTotal = (commissionTotal != null) ? commissionTotal : java.math.BigDecimal.ZERO;

                // Create PENDING payout record: net = order total − commission.
                // Idempotent — skips if a non-cancelled payout already exists for this order.
                payoutService.createForCompletedOrder(saved, safeTotal);

                // Credit the platform wallet with the commission amount (platform's share).
                if (safeTotal.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    platformWalletService.credit(
                            safeTotal,
                            "COMMISSION",
                            saved.getId(),
                            saved.getSellerId(),
                            "Auto credit on order " + saved.getOrderCode());
                }
            } catch (Exception ex) {
                log.warn("Failed to create payout or credit wallet for order {}: {}",
                        saved.getId(), ex.getMessage());
            }
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toDetailDto(saved, items);
    }

    // ---------------- Buyer side ----------------

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderDetailDto createOrderForBuyer(Long userId, CreateOrderRequest request, String idempotencyKey) {
        String resolvedKey = (idempotencyKey == null || idempotencyKey.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKey;

        return idempotencyService.executeOrReplay(
                "order_create",
                resolvedKey,
                idempotencyService.hashRequest(request),
                OrderDetailDto.class,
                () -> doCreateOrder(userId, request, resolvedKey));
    }

    private OrderDetailDto doCreateOrder(Long userId, CreateOrderRequest request, String resolvedKey) {
        // Note: idempotency replay is handled by IdempotencyService.executeOrReplay
        // (the outer layer). The single source of truth for idempotency lives there
        // so that this method runs at most once per (buyer, key).

        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new BadRequestException("cartItemIds must not be empty");
        }
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id: " + request.getAddressId()));
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Address does not belong to the current user");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart not found for user " + userId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId()).stream()
                .filter(ci -> request.getCartItemIds().contains(ci.getId()))
                .toList();
        if (cartItems.isEmpty()) {
            throw new BadRequestException("None of the requested cart items were found in your cart");
        }

        // Group items by seller so that one order only belongs to one seller (simplifies
        // the experiment; in a real B2C we'd split into multiple sub-orders per seller).
        Map<Long, List<CartItem>> bySeller = new HashMap<>();
        for (CartItem ci : cartItems) {
            Long sellerId = catalogFacade.getSellerIdForVariant(ci.getVariantId());
            bySeller.computeIfAbsent(sellerId, k -> new java.util.ArrayList<>())
                    .add(ci);
        }
        if (bySeller.size() > 1) {
            throw new BadRequestException(
                    "Cart items must belong to a single seller for this checkout. "
                            + "Found " + bySeller.size() + " sellers.");
        }
        Long sellerId = bySeller.keySet().iterator().next();

        // Reserve stock for all items BEFORE creating the order — if reservation
        // fails for any item, the entire transaction rolls back.
        for (CartItem ci : cartItems) {
            inventoryPort.reserve(ci.getVariantId(), ci.getQuantity(), null);
        }

        // Compute totals before persisting so subtotal/total are correct on the
        // first INSERT (no need for a follow-up UPDATE).
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            BigDecimal unitPrice = ci.getPriceSnapshot();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        BigDecimal shippingFee = BigDecimal.valueOf(30000);
        BigDecimal discount = BigDecimal.ZERO;
        com.example.B2C.modules.promotion.entity.Promotion appliedPromotion = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            String code = request.getVoucherCode().trim().toUpperCase();
            appliedPromotion = promotionRepository.findByCode(code)
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại: " + code));

            LocalDateTime now = LocalDateTime.now();
            if (appliedPromotion.getStatus() != com.example.B2C.modules.promotion.entity.PromotionStatus.ACTIVE
                    || (appliedPromotion.getStartAt() != null && appliedPromotion.getStartAt().isAfter(now))
                    || (appliedPromotion.getEndAt() != null && appliedPromotion.getEndAt().isBefore(now))) {
                throw new BadRequestException("Mã giảm giá đã hết hạn hoặc không còn hiệu lực.");
            }

            if (appliedPromotion.getQuantity() != null && appliedPromotion.getUsedCount() >= appliedPromotion.getQuantity()) {
                throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng.");
            }

            if (appliedPromotion.getMinOrderValue() != null && subtotal.compareTo(appliedPromotion.getMinOrderValue()) < 0) {
                throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu " + appliedPromotion.getMinOrderValue() + "đ để áp dụng mã.");
            }

            if (appliedPromotion.getSeller() != null && !appliedPromotion.getSeller().getId().equals(sellerId)) {
                throw new BadRequestException("Mã giảm giá chỉ áp dụng cho sản phẩm của shop " + appliedPromotion.getSeller().getShopName());
            }

            discount = promotionService.computeDiscount(appliedPromotion, subtotal);
        }

        BigDecimal total = subtotal.add(shippingFee).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        // Build the order with totals already populated. Note that orderCode uses
        // LocalDateTime.now(); the unique constraint is on orderCode and we accept
        // the negligible collision risk for a human-readable code.
        Order order = Order.builder()
                .orderCode("ORD-" + LocalDateTime.now().getYear() + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .buyerId(userId)
                .sellerId(sellerId)
                .receiverName(address.getRecipientName())
                .receiverPhone(address.getPhone())
                .shippingAddress(address.getFullAddress())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("UNPAID")
                .status(OrderStatus.PENDING)
                .idempotencyKey(resolvedKey)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discount)
                .totalAmount(total)
                .note(request.getNote())
                .build();

        // Persist the order FIRST so it gets an ID we can attach to OrderItems.
        Order saved = orderRepository.save(order);

        // Deduct promotion usage count
        if (appliedPromotion != null) {
            promotionService.incrementUsedCount(appliedPromotion.getId());
        }

        // Build & save OrderItems now that saved.id is known. Use saveAll for a
        // single round-trip, then flush so that a later failure surfaces immediately
        // and the transaction can be rolled back as a unit.
        List<OrderItem> items = new java.util.ArrayList<>(cartItems.size());
        for (CartItem ci : cartItems) {
            var variantView = catalogFacade.getVariant(ci.getVariantId());
            BigDecimal unitPrice = ci.getPriceSnapshot();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            items.add(OrderItem.builder()
                    .orderId(saved.getId())
                    .variantId(variantView.id())
                    .productId(variantView.productId())
                    .productName(variantView.productName())
                    .variantName(variantView.variantName())
                    .imageUrl(variantView.imageUrl())
                    .unitPrice(unitPrice)
                    .quantity(ci.getQuantity())
                    .totalPrice(lineTotal)
                    .isReviewed(false)
                    .build());
        }
        orderItemRepository.saveAll(items);
        orderItemRepository.flush();

        // Reload persisted items for the response DTO (available in all code paths below)
        List<OrderItem> persistedItems = orderItemRepository.findByOrderId(saved.getId());

        // Create pending payment record
        String provider;
        String txnRef = null;
        Payment payment = Payment.builder()
                .order(saved)
                .method(request.getPaymentMethod())
                .provider(null)   // set below after we know which gateway
                .amount(total)
                .status(PaymentTransactionStatus.PENDING)
                .build();

        if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
            // Consume cart items before redirecting to VNPay
            cartItemRepository.deleteAll(cartItems);
            provider = "VNPAY";
            String clientIp = resolveClientIp(request);
            try {
                String paymentUrl = vnpayService.createPaymentUrl(saved, clientIp);
                payment.setProvider(provider);
                paymentRepository.save(payment);
                log.info("VNPay payment URL generated for order {}: {}", saved.getOrderCode(), paymentUrl);
                eventPublisher.publishEvent(new OrderCreatedEvent(this, saved.getId(), userId, sellerId));
                eventPublisher.publishEvent(
                        new com.example.B2C.realtime.event.OrderCreatedEvent(
                                saved.getId(), saved.getOrderCode(), userId, sellerId, total, Instant.now()));
                log.info("Order {} created for buyer {} with total {} (seller {})",
                        saved.getOrderCode(), userId, total, sellerId);
                return toDetailDto(saved, persistedItems, paymentUrl);
            } catch (Exception e) {
                log.error("Failed to generate VNPay URL for order {}: {}", saved.getOrderCode(), e.getMessage(), e);
                // Fall through: keep payment as PENDING, buyer can retry from order detail
                payment.setProvider("VNPAY_PENDING");
                paymentRepository.save(payment);
                eventPublisher.publishEvent(new OrderCreatedEvent(this, saved.getId(), userId, sellerId));
                eventPublisher.publishEvent(
                        new com.example.B2C.realtime.event.OrderCreatedEvent(
                                saved.getId(), saved.getOrderCode(), userId, sellerId, total, Instant.now()));
                log.info("Order {} created for buyer {} with total {} (seller {})",
                        saved.getOrderCode(), userId, total, sellerId);
                return toDetailDto(saved, persistedItems, null);
            }
        } else {
            // COD, MOMO, CARD — no redirect needed
            provider = "MOCK";
            payment.setProvider(provider);
            paymentRepository.save(payment);
        }

        // Consume cart items
        cartItemRepository.deleteAll(cartItems);

        eventPublisher.publishEvent(new OrderCreatedEvent(this, saved.getId(), userId, sellerId));
        eventPublisher.publishEvent(
                new com.example.B2C.realtime.event.OrderCreatedEvent(
                        saved.getId(), saved.getOrderCode(), userId, sellerId, total, Instant.now()));
        log.info("Order {} created for buyer {} with total {} (seller {})",
                saved.getOrderCode(), userId, total, sellerId);
        return toDetailDto(saved, persistedItems);
    }

    @Override
    public PageResponse<OrderSummaryDto> listOrdersForCurrentBuyer(Long userId, OrderStatus status, int page, int size) {
        int p = Math.max(page, 0);
        int s = clampSize(size);
        var pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByBuyer(userId, status, pageable);
        return PageResponse.of(orders.map(OrderSummaryDto::from));
    }

    @Override
    public PageResponse<OrderSummaryDto> listHistoricalBuyerOrders(Long userId, OrderStatus status, int page, int size) {
        // Mirrors listOrdersForCurrentBuyer; kept as a separate method name so the
        // controller endpoint path (/history) is explicit. No role check here —
        // SecurityConfig already restricts the HTTP method to GET for this path.
        return listOrdersForCurrentBuyer(userId, status, page, size);
    }

    @Override
    public OrderDetailDto getOrderForBuyer(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!userId.equals(order.getBuyerId())) {
            throw new ForbiddenException("You do not own this order");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toDetailDto(order, items);
    }

    @Override
    @Transactional
    public OrderDetailDto cancelOrderForBuyer(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!userId.equals(order.getBuyerId())) {
            throw new ForbiddenException("You do not own this order");
        }
        // Idempotent cancel: if the order is already CANCELLED, do not release stock
        // a second time and do not transition state again.
        if (order.getStatus() == OrderStatus.CANCELLED) {
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            return toDetailDto(order, items);
        }
        if (!stateMachine.canCancel(order.getStatus())) {
            throw new BadRequestException(
                    "Order in status " + order.getStatus() + " cannot be cancelled by buyer");
        }
        stateMachine.transition(order, OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(userId);
        order.setCancelReason(reason);
        Order saved = orderRepository.save(order);

        releaseStockForOrder(orderId);
        eventPublisher.publishEvent(new OrderCancelledEvent(this, orderId, userId, order.getSellerId(), reason));
        return toDetailDto(saved, orderItemRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional
    public OrderDetailDto requestReturn(Long userId, Long orderId, ReturnRequestDto request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!userId.equals(order.getBuyerId())) {
            throw new ForbiddenException("You do not own this order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.ORDER_NOT_RETURNABLE);
        }
        // Idempotent: if already requested, return current state
        if (order.getReturnRequestedAt() != null) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_REQUESTED);
        }
        // 7-day window from delivery
        if (order.getDeliveredAt() == null
                || order.getDeliveredAt().isBefore(LocalDateTime.now().minusDays(7))) {
            throw new BusinessException(ErrorCode.RETURN_WINDOW_EXPIRED);
        }
        stateMachine.transition(order, OrderStatus.RETURN_REQUESTED);
        order.setReturnReason(request.getReason());
        order.setReturnDescription(request.getDescription());
        order.setReturnRequestedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(
                new com.example.B2C.realtime.event.ReturnRequestedEvent(
                        order.getId(), order.getOrderCode(), userId,
                        order.getSellerId(), request.getReason(), Instant.now()));
        log.info("Return requested for order {} by user {}", order.getOrderCode(), userId);
        return toDetailDto(saved, orderItemRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional
    public OrderDetailDto approveReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_PROCESSED);
        }
        stateMachine.transition(order, OrderStatus.RETURNED);
        order.setReturnApprovedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        log.info("Return approved for order {}", order.getOrderCode());
        return toDetailDto(saved, orderItemRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional
    public OrderDetailDto rejectReturn(Long orderId, String adminNote) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new BusinessException(ErrorCode.RETURN_ALREADY_PROCESSED);
        }
        stateMachine.transition(order, OrderStatus.DELIVERED);
        order.setReturnRejectedAt(LocalDateTime.now());
        order.setReturnAdminNote(adminNote);
        Order saved = orderRepository.save(order);
        log.info("Return rejected for order {} by admin", order.getOrderCode());
        return toDetailDto(saved, orderItemRepository.findByOrderId(orderId));
    }

    // ---------------- Admin side ----------------

    @Override
    public PageResponse<OrderSummaryDto> listOrdersForAdmin(OrderStatus status, int page, int size) {
        int p = Math.max(page, 0);
        int s = clampSize(size);
        var pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findAllAdmin(status, pageable);
        return PageResponse.of(orders.map(OrderSummaryDto::from));
    }

    @Override
    public OrderDetailDto getOrderForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toDetailDto(order, items);
    }

    // ---------------- Helpers ----------------

    private OrderDetailDto toDetailDto(Order order, List<OrderItem> items) {
        return toDetailDto(order, items, null);
    }

    private OrderDetailDto toDetailDto(Order order, List<OrderItem> items, String paymentUrl) {
        OrderDetailDto dto = OrderDetailDto.from(order, items, paymentUrl);
        if (dto.getItems() != null) {
            for (OrderItemDto item : dto.getItems()) {
                if ((item.getImageUrl() == null || item.getImageUrl().isBlank()) && item.getVariantId() != null) {
                    try {
                        item.setImageUrl(catalogFacade.getImageUrl(item.getVariantId()));
                    } catch (Exception ignored) {}
                }
            }
        }
        return dto;
    }

    private void releaseStockForOrder(Long orderId) {
        // InventoryPort.release() now dispatches internally: it releases from
        // reservedQuantity when stock is held by an unpaid order, or from soldCount
        // when the order was already confirmed. Either way the invariant
        //   stockQuantity + reservedQuantity + soldCount = initial stock
        // is preserved per variant.
        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
            try {
                inventoryPort.release(item.getVariantId(), item.getQuantity());
            } catch (RuntimeException ex) {
                log.warn("Failed to release stock for variant {} qty {}: {}",
                        item.getVariantId(), item.getQuantity(), ex.getMessage());
            }
        }
    }

    private Long getActiveSellerId(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Seller profile is not active");
        }
        return seller.getId();
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 100);
    }

    private String resolveClientIp(CreateOrderRequest request) {
        // IP is passed through the DTO or falls back to a default
        if (request.getClientIp() != null && !request.getClientIp().isBlank()) {
            return request.getClientIp();
        }
        return "127.0.0.1";
    }
}
