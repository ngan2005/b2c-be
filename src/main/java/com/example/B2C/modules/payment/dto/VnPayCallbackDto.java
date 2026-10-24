package com.example.B2C.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VnPayCallbackDto {
    private boolean success;
    private String txnRef;
    private String orderCode;
    private BigDecimal amount;
    private String responseCode;
    private String transactionNo;
    private String bankCode;
    private String payDate;
    private String secureHash;
    private Map<String, String> rawParams;
}
