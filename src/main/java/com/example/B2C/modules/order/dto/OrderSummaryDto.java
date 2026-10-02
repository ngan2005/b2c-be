package com.example.B2C.modules.order.dto;

import com.example.B2C.modules.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDto {
    private Long id;
    private String orderCode;
    private Long buyerId;
    private Long sellerId;
    private String receiverName;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public static OrderSummaryDto from(Order order) {
        return OrderSummaryDto.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .receiverName(order.getReceiverName())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}