package com.example.B2C.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String transactionId;

    @NotNull
    private String status; // "SUCCESS" | "FAILED"

    private String provider;
    private String signature;
}
