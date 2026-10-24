package com.example.B2C.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull
    private List<Long> cartItemIds;

    @NotNull
    private Long addressId;

    @NotNull
    private String paymentMethod;

    private String voucherCode;

    private String note;

    /** Client IP address forwarded by the API gateway or load balancer. */
    private String clientIp;
}
