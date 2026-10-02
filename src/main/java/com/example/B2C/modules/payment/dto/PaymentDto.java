package com.example.B2C.modules.payment.dto;

import com.example.B2C.modules.payment.entity.PaymentTransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private Long orderId;
    private String method;
    private String provider;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private PaymentTransactionStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private BigDecimal refundAmount;
}
