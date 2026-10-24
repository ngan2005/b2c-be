package com.example.B2C.modules.payment.service;

import com.example.B2C.common.event.OrderCancelledEvent;
import com.example.B2C.common.event.OrderPaidEvent;
import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.order.service.OrderStateMachine;
import com.example.B2C.modules.payment.dto.PaymentDto;
import com.example.B2C.modules.payment.entity.Payment;
import com.example.B2C.modules.payment.entity.PaymentTransactionStatus;
import com.example.B2C.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStateMachine stateMachine;
    private final InventoryPort inventoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<PaymentDto> listForOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(PaymentService::toDto)
                .toList();
    }

    /**
     * Confirms a payment for the given order, identified by the gateway transaction id.
     * Idempotent: replays return the existing payment without re-applying side effects.
     *
     * @return the resulting {@link PaymentDto}
     */
    @Transactional
    public PaymentDto confirmPayment(Long orderId, String transactionId, String status,
                                     String provider, String idempotencyKey) {
        var existing = paymentRepository.findByOrderIdAndTransactionId(orderId, transactionId);
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getStatus() == PaymentTransactionStatus.SUCCESS
                    || p.getStatus() == PaymentTransactionStatus.FAILED) {
                log.info("Replay payment callback for order {} txn {} -> {}", orderId, transactionId, p.getStatus());
                return toDto(p);
            }
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id: " + orderId));
        Payment payment = existing.orElseGet(() -> Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .build());
        payment.setTransactionId(transactionId);
        payment.setIdempotencyKey(idempotencyKey);
        if (provider != null) {
            payment.setProvider(provider);
        }
        boolean success = "SUCCESS".equalsIgnoreCase(status);
        boolean failed = "FAILED".equalsIgnoreCase(status);
        if (!success && !failed) {
            throw new BadRequestException("Invalid payment status: " + status);
        }

        if (success) {
            payment.setStatus(PaymentTransactionStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            // Confirm stock: reserved -> sold
            for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
                inventoryPort.confirm(item.getVariantId(), item.getQuantity());
            }

            order.setPaymentStatus("PAID");
            stateMachine.transition(order, OrderStatus.CONFIRMED);
            orderRepository.save(order);
            paymentRepository.save(payment);

            eventPublisher.publishEvent(new OrderPaidEvent(this, orderId, order.getBuyerId(), order.getSellerId()));
            log.info("Payment SUCCESS for order {} via txn {}", orderId, transactionId);
        } else {
            payment.setStatus(PaymentTransactionStatus.FAILED);
            paymentRepository.save(payment);

            // Release stock back
            for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
                inventoryPort.release(item.getVariantId(), item.getQuantity());
            }
            stateMachine.transition(order, OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancelReason("Payment failed");
            orderRepository.save(order);

            eventPublisher.publishEvent(new OrderCancelledEvent(this, orderId, order.getBuyerId(),
                    order.getSellerId(), "Payment failed"));
            log.info("Payment FAILED for order {} via txn {}", orderId, transactionId);
        }
        return toDto(payment);
    }

    /**
     * Confirms a payment using just the VNPay transaction reference (vnp_TxnRef).
     * Used by the /return endpoint which only has the txnRef from VNPay, not the internal orderId.
     *
     * @param txnRef VNPay's transaction reference (stored as vnp_TxnRef, maps to our orderCode_txid)
     * @param transactionNo VNPay's transaction number
     * @param responseCode  VNPay response code; "00" means success
     * @param idempotencyKey idempotency key for replay safety
     * @return the resulting PaymentDto
     */
    @Transactional
    public PaymentDto confirmPaymentByTxnRef(String txnRef, String transactionNo,
                                            String responseCode, String idempotencyKey) {
        var existing = paymentRepository.findByTransactionId(txnRef);
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getStatus() == PaymentTransactionStatus.SUCCESS
                    || p.getStatus() == PaymentTransactionStatus.FAILED) {
                log.info("Replay VNPay callback for txnRef {} -> {}", txnRef, p.getStatus());
                return toDto(p);
            }
        }

        // Resolve order from the txnRef prefix (orderCode_txid format)
        String orderCode = txnRef.contains("_")
                ? txnRef.substring(0, txnRef.lastIndexOf('_'))
                : txnRef;

        var orderOpt = orderRepository.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            log.warn("VNPay return: no order found for orderCode={} (txnRef={})", orderCode, txnRef);
            throw new ResourceNotFoundException("Order", "orderCode: " + orderCode);
        }
        Order order = orderOpt.get();

        Payment payment = existing.orElseGet(() -> Payment.builder()
                .order(order)
                .method(order.getPaymentMethod())
                .provider("VNPAY")
                .amount(order.getTotalAmount())
                .build());
        payment.setTransactionId(txnRef);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setProvider("VNPAY");

        boolean success = "00".equals(responseCode);

        if (success) {
            payment.setStatus(PaymentTransactionStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
                inventoryPort.confirm(item.getVariantId(), item.getQuantity());
            }

            order.setPaymentStatus("PAID");
            stateMachine.transition(order, OrderStatus.CONFIRMED);
            orderRepository.save(order);
            paymentRepository.save(payment);

            eventPublisher.publishEvent(new OrderPaidEvent(this, order.getId(), order.getBuyerId(), order.getSellerId()));
            log.info("VNPay payment SUCCESS for order {} txnRef {}", order.getOrderCode(), txnRef);
        } else {
            payment.setStatus(PaymentTransactionStatus.FAILED);
            paymentRepository.save(payment);

            for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
                inventoryPort.release(item.getVariantId(), item.getQuantity());
            }
            stateMachine.transition(order, OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            order.setCancelReason("Payment failed (VNPay response code: " + responseCode + ")");
            orderRepository.save(order);

            eventPublisher.publishEvent(new OrderCancelledEvent(this, order.getId(), order.getBuyerId(),
                    order.getSellerId(), "Payment failed"));
            log.info("VNPay payment FAILED for order {} txnRef {} responseCode {}", order.getOrderCode(), txnRef, responseCode);
        }
        return toDto(payment);
    }

    static PaymentDto toDto(Payment p) {
        return PaymentDto.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .method(p.getMethod())
                .provider(p.getProvider())
                .transactionId(p.getTransactionId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .refundedAt(p.getRefundedAt())
                .refundAmount(p.getRefundAmount())
                .build();
    }
}
