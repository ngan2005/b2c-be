package com.example.B2C.modules.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class PreviewOrderRequest {
    private List<Long> cartItemIds;
    private Long addressId;
    private String voucherCode;
}

