package com.example.B2C.modules.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VnPayConfig {
    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    @Value("${vnpay.pay-url}")
    private String payUrl;
    @Value("${vnpay.return-url}")
    private String returnUrl;
    @Value("${vnpay.api-url}")
    private String apiUrl;
    @Value("${vnpay.version}")
    private String version;
    @Value("${vnpay.command}")
    private String command;
    @Value("${vnpay.locale}")
    private String locale;
    @Value("${vnpay.currency-code}")
    private String currencyCode;
}
