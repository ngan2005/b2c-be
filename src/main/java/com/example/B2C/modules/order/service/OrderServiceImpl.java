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
        if (newStatus == OrderStatus.RETURNED) {
            releaseStockForOrder(orderId);
        }
        if (newStatus == OrderStatus.SHIPPING) {
            eventPublisher.publishEvent(new OrderShippedEvent(this, orderId, order.getBuyerId(), sellerId));
        }

        // ── Commission snapshot & Stock confirmation on COMPLETED ──
        // Order completion is the source of truth; commission records are derived.
        // Failures here must NOT break order completion — log and continue.
        if (newStatus == OrderStatus.COMPLETED) {
            if (!"PAID".equalsIgnoreCase(saved.getPaymentStatus())) {
                saved.setPaymentStatus("PAID");
                saved = orderRepository.save(saved);
                confirmStockForOrder(orderId);
            }

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

        // Group items by seller so that in a real B2C marketplace we split into multiple sub-orders per seller
        Map<Long, List<CartItem>> bySeller = new java.util.LinkedHashMap<>();
        for (CartItem ci : cartItems) {
            Long sellerId = catalogFacade.getSellerIdForVariant(ci.getVariantId());
            bySeller.computeIfAbsent(sellerId, k -> new java.util.ArrayList<>())
                    .add(ci);
        }

        // Reserve stock for all items across all sellers BEFORE creating orders
        for (CartItem ci : cartItems) {
            inventoryPort.reserve(ci.getVariantId(), ci.getQuantity(), null);
        }

        List<Order> createdOrders = new java.util.ArrayList<>();
        List<List<OrderItem>> createdOrderItems = new java.util.ArrayList<>();
        String primaryPaymentUrl = null;

        // Process promotion if any (applies to first eligible order or platform discount)
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
        }

        int sellerIndex = 0;
        for (Map.Entry<Long, List<CartItem>> entry : bySeller.entrySet()) {
            Long sId = entry.getKey();
            List<CartItem> sItems = entry.getValue();

            BigDecimal sSubtotal = BigDecimal.ZERO;
            for (CartItem ci : sItems) {
                BigDecimal unitPrice = ci.getPriceSnapshot();
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
                sSubtotal = sSubtotal.add(lineTotal);
            }

            BigDecimal sShippingFee = calculateShippingFee(sId, address, sItems);
            BigDecimal sDiscount = BigDecimal.ZERO;

            if (appliedPromotion != null) {
                boolean eligibleForSeller = (appliedPromotion.getSeller() == null || appliedPromotion.getSeller().getId().equals(sId));
                if (eligibleForSeller && (appliedPromotion.getMinOrderValue() == null || sSubtotal.compareTo(appliedPromotion.getMinOrderValue()) >= 0)) {
                    sDiscount = promotionService.computeDiscount(appliedPromotion, sSubtotal);
                    promotionService.incrementUsedCount(appliedPromotion.getId());
                    appliedPromotion = null; // Used once
                }
            }

            BigDecimal sTotal = sSubtotal.add(sShippingFee).subtract(sDiscount);
            if (sTotal.compareTo(BigDecimal.ZERO) < 0) {
                sTotal = BigDecimal.ZERO;
            }

            String sOrderCode = "ORD-" + LocalDateTime.now().getYear() + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String sIdempotencyKey = (sellerIndex == 0) ? resolvedKey : (resolvedKey + "-S" + sellerIndex);

            Order sOrder = Order.builder()
                    .orderCode(sOrderCode)
                    .buyerId(userId)
                    .sellerId(sId)
                    .receiverName(address.getRecipientName())
                    .receiverPhone(address.getPhone())
                    .shippingAddress(address.getFullAddress())
                    .paymentMethod(request.getPaymentMethod())
                    .paymentStatus("UNPAID")
                    .status(OrderStatus.PENDING)
                    .idempotencyKey(sIdempotencyKey)
                    .subtotal(sSubtotal)
                    .shippingFee(sShippingFee)
                    .discountAmount(sDiscount)
                    .totalAmount(sTotal)
                    .note(request.getNote())
                    .build();

            Order savedOrder = orderRepository.save(sOrder);
            createdOrders.add(savedOrder);

            List<OrderItem> oItems = new java.util.ArrayList<>(sItems.size());
            for (CartItem ci : sItems) {
                var variantView = catalogFacade.getVariant(ci.getVariantId());
                BigDecimal unitPrice = ci.getPriceSnapshot();
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));

                String variantDisplay = variantView.variantName();
                int weight = catalogFacade.getWeightGram(variantView.id());
                if (variantDisplay != null && !variantDisplay.isBlank()) {
                    if (weight > 0 && !variantDisplay.contains("g")) {
                        variantDisplay = variantDisplay + " (" + weight + "g)";
                    }
                } else {
                    variantDisplay = (weight > 0) ? ("Mặc định (" + weight + "g)") : "Mặc định";
                }

                oItems.add(OrderItem.builder()
                        .orderId(savedOrder.getId())
                        .variantId(variantView.id())
                        .productId(variantView.productId())
                        .productName(variantView.productName())
                        .variantName(variantDisplay)
                        .imageUrl(variantView.imageUrl())
                        .unitPrice(unitPrice)
                        .quantity(ci.getQuantity())
                        .totalPrice(lineTotal)
                        .isReviewed(false)
                        .build());
            }
            orderItemRepository.saveAll(oItems);
            orderItemRepository.flush();
            createdOrderItems.add(oItems);

            Payment payment = Payment.builder()
                    .order(savedOrder)
                    .method(request.getPaymentMethod())
                    .amount(sTotal)
                    .status(PaymentTransactionStatus.PENDING)
                    .build();

            if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
                String clientIp = resolveClientIp(request);
                try {
                    String paymentUrl = vnpayService.createPaymentUrl(savedOrder, clientIp);
                    payment.setProvider("VNPAY");
                    paymentRepository.save(payment);
                    if (primaryPaymentUrl == null) primaryPaymentUrl = paymentUrl;
                } catch (Exception e) {
                    payment.setProvider("VNPAY_PENDING");
                    paymentRepository.save(payment);
                }
            } else {
                payment.setProvider("MOCK");
                paymentRepository.save(payment);
            }

            eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder.getId(), userId, sId));
            eventPublisher.publishEvent(
                    new com.example.B2C.realtime.event.OrderCreatedEvent(
                            savedOrder.getId(), savedOrder.getOrderCode(), userId, sId, sTotal, Instant.now()));

            log.info("Sub-order {} created for seller {} (buyer {}, total {})",
                    savedOrder.getOrderCode(), sId, userId, sTotal);
            sellerIndex++;
        }

        // Consume all checked-out cart items across all sellers
        cartItemRepository.deleteAll(cartItems);

        // Return details of primary order (or first order)
        Order primaryOrder = createdOrders.get(0);
        List<OrderItem> primaryItems = createdOrderItems.get(0);
        return (primaryPaymentUrl != null)
                ? toDetailDto(primaryOrder, primaryItems, primaryPaymentUrl)
                : toDetailDto(primaryOrder, primaryItems);
    }

    @Override
    public PageResponse<OrderSummaryDto> listOrdersForCurrentBuyer(Long userId, OrderStatus status, int page, int size) {
        int p = Math.max(page, 0);
        int s = clampSize(size);
        var pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByBuyer(userId, status, pageable);
        return PageResponse.of(orders.map(o -> enrichSummary(o, OrderSummaryDto.from(o))));
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
        releaseStockForOrder(orderId);
        log.info("Return approved and stock released for order {}", order.getOrderCode());
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

    private void confirmStockForOrder(Long orderId) {
        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
            try {
                inventoryPort.confirm(item.getVariantId(), item.getQuantity());
            } catch (RuntimeException ex) {
                log.warn("Failed to confirm stock for variant {} qty {}: {}",
                        item.getVariantId(), item.getQuantity(), ex.getMessage());
            }
        }
    }

    private void releaseStockForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        boolean wasPaid = order != null && "PAID".equalsIgnoreCase(order.getPaymentStatus());
        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
            try {
                if (wasPaid) {
                    inventoryPort.releaseSold(item.getVariantId(), item.getQuantity());
                } else {
                    inventoryPort.releaseReserved(item.getVariantId(), item.getQuantity());
                }
            } catch (RuntimeException ex) {
                log.warn("Failed to release stock for variant {} qty {} (wasPaid={}): {}",
                        item.getVariantId(), item.getQuantity(), wasPaid, ex.getMessage());
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

    // ── P0.2: Enrich OrderSummaryDto with seller + first-item display info ──────

    private OrderSummaryDto enrichSummary(Order order, OrderSummaryDto dto) {
        try {
            sellerRepository.findById(order.getSellerId()).ifPresent(s -> dto.setShopName(s.getShopName()));
        } catch (Exception ignored) {}
        try {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            if (!items.isEmpty()) {
                OrderItem first = items.get(0);
                dto.setFirstProductName(first.getProductName());
                dto.setFirstProductThumbnail(first.getImageUrl());
                dto.setFirstVariantName(first.getVariantName());
                dto.setTotalItemsCount(items.size());
            }
        } catch (Exception ignored) {}
        return dto;
    }

    // ── P1.2: Retry payment for buyer ──────────────────────────────────────────

    @Override
    @Transactional
    public String retryPaymentForBuyer(Long userId, Long orderId, String clientIp) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!userId.equals(order.getBuyerId())) {
            throw new ForbiddenException("You do not own this order");
        }
        if (order.getStatus() != OrderStatus.PENDING || !"UNPAID".equals(order.getPaymentStatus())) {
            throw new BadRequestException("Order is not in a payable state (must be PENDING + UNPAID)");
        }
        String ip = (clientIp != null && !clientIp.isBlank()) ? clientIp : "127.0.0.1";
        return vnpayService.createPaymentUrl(order, ip);
    }

    // ── P2.1: Preview order (in-memory, no DB write) ────────────────────────────

    @Override
    public com.example.B2C.modules.order.dto.OrderPreviewResponse previewOrder(
            Long userId,
            com.example.B2C.modules.order.dto.PreviewOrderRequest req) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId()).stream()
                .filter(ci -> req.getCartItemIds() != null && req.getCartItemIds().contains(ci.getId()))
                .toList();
        if (items.isEmpty()) {
            throw new BadRequestException("No matching cart items found");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : items) {
            BigDecimal unit = ci.getPriceSnapshot() != null ? ci.getPriceSnapshot() : BigDecimal.ZERO;
            subtotal = subtotal.add(unit.multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // Group items by seller to calculate shipping fee accurately per shop package
        Map<Long, List<CartItem>> bySeller = new java.util.HashMap<>();
        for (CartItem ci : items) {
            Long sId = catalogFacade.getSellerIdForVariant(ci.getVariantId());
            bySeller.computeIfAbsent(sId, k -> new java.util.ArrayList<>()).add(ci);
        }

        Address buyerAddress = (req.getAddressId() != null)
                ? addressRepository.findById(req.getAddressId()).orElse(null)
                : null;
        BigDecimal shippingFee = BigDecimal.ZERO;
        for (Map.Entry<Long, List<CartItem>> entry : bySeller.entrySet()) {
            shippingFee = shippingFee.add(calculateShippingFee(entry.getKey(), buyerAddress, entry.getValue()));
        }
        BigDecimal discount = BigDecimal.ZERO;

        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            String code = req.getVoucherCode().trim().toUpperCase();
            try {
                var promo = promotionRepository.findByCode(code).orElse(null);
                if (promo != null
                        && promo.getStatus() == com.example.B2C.modules.promotion.entity.PromotionStatus.ACTIVE) {
                    discount = promotionService.computeDiscount(promo, subtotal);
                }
            } catch (Exception ignored) {}
        }

        BigDecimal total = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        return com.example.B2C.modules.order.dto.OrderPreviewResponse.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discount)
                .totalAmount(total)
                .build();
    }

    /**
     * Dynamic weight-based and location-based shipping fee calculation.
     * Tiered pricing:
     * - Same province: 20,000 VND (up to 1kg), +3,000 VND per extra 500g
     * - Different province: 35,000 VND (up to 1kg), +5,000 VND per extra 500g
     */
    private BigDecimal calculateShippingFee(Long sellerId, Address buyerAddress, List<CartItem> cartItems) {
        int totalWeightGram = 0;
        for (CartItem ci : cartItems) {
            int unitWeight = catalogFacade.getWeightGram(ci.getVariantId());
            totalWeightGram += unitWeight * ci.getQuantity();
        }

        boolean isSameProvince = false;
        if (buyerAddress != null && sellerId != null) {
            Seller seller = sellerRepository.findById(sellerId).orElse(null);
            if (seller != null && seller.getPickupAddress() != null) {
                String sellerProvince = seller.getPickupAddress().getProvinceCode();
                String buyerProvince = buyerAddress.getProvinceCode();
                if (sellerProvince != null && buyerProvince != null && !sellerProvince.isBlank()) {
                    isSameProvince = sellerProvince.trim().equalsIgnoreCase(buyerProvince.trim());
                } else if (seller.getPickupAddress().getFullAddress() != null && buyerAddress.getFullAddress() != null) {
                    String sAddr = seller.getPickupAddress().getFullAddress().toLowerCase();
                    String bAddr = buyerAddress.getFullAddress().toLowerCase();
                    isSameProvince = (sAddr.contains("hồ chí minh") && bAddr.contains("hồ chí minh"))
                            || (sAddr.contains("hà nội") && bAddr.contains("hà nội"))
                            || (sAddr.contains("đà nẵng") && bAddr.contains("đà nẵng"));
                }
            }
        }

        long baseFee = isSameProvince ? 20_000L : 35_000L;
        long extraRate = isSameProvince ? 3_000L : 5_000L;

        if (totalWeightGram > 1000) {
            int extraGrams = totalWeightGram - 1000;
            long extraSteps = (extraGrams + 499) / 500;
            baseFee += extraSteps * extraRate;
        }

        return BigDecimal.valueOf(baseFee);
    }
}
