package com.example.B2C.modules.order.service;

import com.example.B2C.common.event.OrderCancelledEvent;
import com.example.B2C.common.idempotency.IdempotencyService;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.modules.cart.repository.CartItemRepository;
import com.example.B2C.modules.cart.repository.CartRepository;
import com.example.B2C.modules.catalog.api.CatalogFacade;
import com.example.B2C.modules.finance.repository.CommissionRecordRepository;
import com.example.B2C.modules.finance.service.CommissionService;
import com.example.B2C.modules.finance.service.PayoutService;
import com.example.B2C.modules.finance.service.PlatformWalletService;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.order.scheduler.UnpaidOrderCleanupScheduler;
import com.example.B2C.modules.payment.config.VnPayConfig;
import com.example.B2C.modules.payment.repository.PaymentRepository;
import com.example.B2C.modules.payment.service.VnPayService;
import com.example.B2C.modules.promotion.repository.PromotionRepository;
import com.example.B2C.modules.promotion.service.PromotionService;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnpaidOrderCleanupTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private CatalogFacade catalogFacade;
    @Mock private AddressRepository addressRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private InventoryPort inventoryPort;
    @Mock private IdempotencyService idempotencyService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private OrderStateMachine stateMachine;
    @Mock private VnPayService vnpayService;
    @Mock private VnPayConfig vnpayConfig;
    @Mock private CommissionService commissionService;
    @Mock private PayoutService payoutService;
    @Mock private PlatformWalletService platformWalletService;
    @Mock private CommissionRecordRepository commissionRecordRepository;
    @Mock private PromotionRepository promotionRepository;
    @Mock private PromotionService promotionService;

    private OrderServiceImpl orderService;
    private Order expiredOrder;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository, orderItemRepository, sellerRepository, cartRepository,
                cartItemRepository, catalogFacade, addressRepository, paymentRepository,
                inventoryPort, idempotencyService, eventPublisher, stateMachine,
                vnpayService, vnpayConfig, commissionService, payoutService,
                platformWalletService, commissionRecordRepository, promotionRepository,
                promotionService
        );
        ReflectionTestUtils.setField(orderService, "unpaidTtlMinutes", 15);

        expiredOrder = Order.builder()
                .id(100L)
                .orderCode("ORD-EXP-001")
                .buyerId(20L)
                .sellerId(5L)
                .status(OrderStatus.PENDING)
                .paymentMethod("VNPAY")
                .paymentStatus("UNPAID")
                .totalAmount(BigDecimal.valueOf(500_000))
                .build();
        expiredOrder.setCreatedAt(LocalDateTime.now().minusMinutes(20));

        item = OrderItem.builder()
                .id(200L)
                .orderId(100L)
                .variantId(55L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(500_000))
                .build();
    }

    @Test
    @DisplayName("cancelExpiredUnpaidOrders: auto cancels order, releases reserved inventory, and publishes event")
    void cancelExpiredUnpaidOrders_cancelsAndReleasesStock() {
        when(orderRepository.findExpiredUnpaidOrders(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredOrder));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(expiredOrder));
        when(orderItemRepository.findByOrderId(100L)).thenReturn(List.of(item));

        int cancelled = orderService.cancelExpiredUnpaidOrders();

        assertThat(cancelled).isEqualTo(1);
        verify(stateMachine).transition(expiredOrder, OrderStatus.CANCELLED);
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(expiredOrder.getCancelReason()).contains("Hết thời gian thanh toán");
        verify(orderRepository).save(expiredOrder);

        // Verify reserved stock was released
        verify(inventoryPort).releaseReserved(55L, 2);

        // Verify cancelled event was published
        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderCancelledEvent captured = eventCaptor.getValue();
        assertThat(captured.getOrderId()).isEqualTo(100L);
        assertThat(captured.getBuyerId()).isEqualTo(20L);
        assertThat(captured.getSellerId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("UnpaidOrderCleanupScheduler: triggers order cancellation periodically")
    void scheduler_invokesCleanup() {
        OrderService mockService = mock(OrderService.class);
        when(mockService.cancelExpiredUnpaidOrders()).thenReturn(3);

        UnpaidOrderCleanupScheduler scheduler = new UnpaidOrderCleanupScheduler(mockService);
        scheduler.cleanupExpiredUnpaidOrders();

        verify(mockService, times(1)).cancelExpiredUnpaidOrders();
    }
}
