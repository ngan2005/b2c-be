package com.example.B2C.modules.order.service;

import com.example.B2C.common.event.OrderCancelledEvent;
import com.example.B2C.common.event.OrderCreatedEvent;
import com.example.B2C.common.event.OrderPaidEvent;
import com.example.B2C.common.event.OrderShippedEvent;
import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.idempotency.IdempotencyService;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.cart.entity.Cart;
import com.example.B2C.modules.cart.entity.CartItem;
import com.example.B2C.modules.cart.repository.CartRepository;
import com.example.B2C.modules.cart.service.CartService;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderItemDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.payment.entity.Payment;
import com.example.B2C.modules.payment.entity.PaymentTransactionStatus;
import com.example.B2C.modules.payment.repository.PaymentRepository;
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
    private final ProductVariantRepository variantRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryPort inventoryPort;
    private final IdempotencyService idempotencyService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderStateMachine stateMachine;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING,   Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED),
            OrderStatus.PACKED,    Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING,  Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNED),
            OrderStatus.DELIVERED, Set.of(OrderStatus.COMPLETED, OrderStatus.RETURNED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of(),
            OrderStatus.RETURNED,  Set.of()
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
        return OrderDetailDto.from(order, items);
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

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return OrderDetailDto.from(saved, items);
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
                () -> doCreateOrder(userId, request, resolvedKey));
    }

    private OrderDetailDto doCreateOrder(Long userId, CreateOrderRequest request, String resolvedKey) {
        // Replay path: if an order already exists with this key, return it
        var existing = orderRepository.findByBuyerIdAndIdempotencyKey(userId, resolvedKey);
        if (existing.isPresent()) {
            Order previous = existing.get();
            return OrderDetailDto.from(previous, orderItemRepository.findByOrderId(previous.getId()));
        }

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
            ProductVariant variant = ci.getVariant();
            bySeller.computeIfAbsent(variant.getProduct().getSeller().getId(), k -> new java.util.ArrayList<>())
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
            inventoryPort.reserve(ci.getVariant().getId(), ci.getQuantity(), null);
        }

        // Build order + items
        BigDecimal subtotal = BigDecimal.ZERO;
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
                .note(request.getNote())
                .build();

        for (CartItem ci : cartItems) {
            ProductVariant variant = ci.getVariant();
            BigDecimal unitPrice = ci.getPriceSnapshot();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            orderItemRepository.save(OrderItem.builder()
                    .orderId(order.getId())
                    .variantId(variant.getId())
                    .productId(variant.getProduct().getId())
                    .productName(variant.getProduct().getName())
                    .variantName(variant.getVariantName())
                    .imageUrl(variant.getImageUrl())
                    .unitPrice(unitPrice)
                    .quantity(ci.getQuantity())
                    .totalPrice(lineTotal)
                    .isReviewed(false)
                    .build());
        }
        BigDecimal shippingFee = BigDecimal.valueOf(30000);
        BigDecimal discount = BigDecimal.ZERO; // Promotion port to be added later
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discount);
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // After save, the items need a valid order_id — re-save them now
        List<OrderItem> items = orderItemRepository.findByOrderId(saved.getId());

        // Create pending payment record
        Payment payment = Payment.builder()
                .order(saved)
                .method(request.getPaymentMethod())
                .provider("MOCK")
                .amount(total)
                .status(PaymentTransactionStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Consume cart items
        cartItemRepository.deleteAll(cartItems);

        eventPublisher.publishEvent(new OrderCreatedEvent(this, saved.getId(), userId, sellerId));
        log.info("Order {} created for buyer {} with total {} (seller {})",
                saved.getOrderCode(), userId, total, sellerId);
        return OrderDetailDto.from(saved, items);
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
    public OrderDetailDto getOrderForBuyer(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!userId.equals(order.getBuyerId())) {
            throw new ForbiddenException("You do not own this order");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return OrderDetailDto.from(order, items);
    }

    @Override
    @Transactional
    public OrderDetailDto cancelOrderForBuyer(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        if (!userId.equals(order.getBuyerId())) {
            throw new ForbiddenException("You do not own this order");
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
        return OrderDetailDto.from(saved, orderItemRepository.findByOrderId(orderId));
    }

    // ---------------- Helpers ----------------

    private void releaseStockForOrder(Long orderId) {
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
}
