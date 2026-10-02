package com.example.B2C.modules.order.dto;

import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {

    private Long id;
    private String orderCode;
    private Long buyerId;
    private Long sellerId;

    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;

    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal platformDiscount;
    private BigDecimal totalAmount;

    private String paymentMethod;
    private String paymentStatus;
    private String status;

    private String note;
    private String cancelReason;

    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItemDto> items;

    public static OrderDetailDto from(Order order, List<OrderItem> items) {
        return OrderDetailDto.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .platformDiscount(order.getPlatformDiscount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .status(order.getStatus().name())
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items.stream().map(OrderItemDto::from).collect(Collectors.toList()))
                .build();
    }
}