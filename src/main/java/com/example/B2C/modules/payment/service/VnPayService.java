package com.example.B2C.modules.payment.service;

import com.example.B2C.modules.payment.config.VnPayConfig;
import com.example.B2C.modules.payment.dto.VnPayCallbackDto;
import com.example.B2C.modules.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String STRING_EMPTY = "";
    private static final String AMPERSAND = "&";

    private final VnPayConfig config;

    /**
     * Builds a VNPay payment URL for the given order.
     *
     * @param order    the placed order
     * @param clientIp IP address of the requesting client
     * @return the full redirect URL pointing to the VNPay sandbox/payment page
     */
    public String createPaymentUrl(Order order, String clientIp) {
        String txnRef = order.getOrderCode() + "_" + System.currentTimeMillis();
        String amountStr = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString();

        Map<String, String> params = new TreeMap<>();  // TreeMap keeps keys alphabetically sorted
        params.put("vnp_Amount", amountStr);
        params.put("vnp_Command", config.getCommand());
        params.put("vnp_CurrCode", config.getCurrencyCode());
        params.put("vnp_Locale", config.getLocale());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Version", config.getVersion());
        params.put("vnp_IpAddr", clientIp);

        // Optional: bank code if known
        // params.put("vnp_BankCode", bankCode);

        // Build the hash data string (key1=value1&key2=value2&...)
        StringBuilder hashData = new StringBuilder();
        StringBuilder queryString = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey = urlEncode(entry.getKey());
            String encodedValue = urlEncode(entry.getValue());

            hashData.append(encodedKey).append("=").append(encodedValue).append(AMPERSAND);
            queryString.append(encodedKey).append("=").append(encodedValue).append(AMPERSAND);
        }

        // Remove trailing &
        String hashInput = hashData.substring(0, hashData.length() - 1);
        String secureHash = hmacSha512(hashInput, config.getHashSecret());

        queryString.append("vnp_SecureHashType=HmacSHA512");
        queryString.append(AMPERSAND);
        queryString.append("vnp_SecureHash=").append(secureHash);

        return config.getPayUrl() + "?" + queryString;
    }

    /**
     * Verifies the VNPay callback signature.
     *
     * @param params all query parameters from the VNPay return/callback URL
     * @return true if the SecureHash matches, false otherwise
     */
    public boolean verifyCallback(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        // VNPay sends these two fields; we compute hash from everything else
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) {
            return false;
        }

        // Collect all vnp_* fields except vnp_SecureHash and vnp_SecureHashType
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("vnp_")
                    && !"vnp_SecureHash".equals(entry.getKey())
                    && !"vnp_SecureHashType".equals(entry.getKey())
                    && entry.getValue() != null
                    && !entry.getValue().isBlank()) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        // Build hash input in alphabetical order
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            hashData.append(urlEncode(entry.getKey()))
                    .append("=")
                    .append(urlEncode(entry.getValue()))
                    .append(AMPERSAND);
        }
        // Remove trailing &
        String hashInput = hashData.substring(0, hashData.length() - 1);

        String computedHash = hmacSha512(hashInput, config.getHashSecret());

        boolean valid = computedHash.equalsIgnoreCase(receivedHash);
        if (!valid) {
            log.warn("VNPay callback signature mismatch. Received={} Computed={}", receivedHash, computedHash);
        }
        return valid;
    }

    /**
     * Parses VNPay callback/query parameters into a structured DTO.
     * The signature is NOT verified here — call {@link #verifyCallback} first.
     *
     * @param params all query parameters from the VNPay return/callback URL
     * @return VnPayCallbackDto with parsed fields
     */
    public VnPayCallbackDto parseCallbackResponse(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        String orderCode = txnRef != null && txnRef.contains("_")
                ? txnRef.substring(0, txnRef.lastIndexOf('_'))
                : txnRef;

        String amountStr = params.get("vnp_Amount");
        BigDecimal amount = BigDecimal.ZERO;
        if (amountStr != null) {
            try {
                amount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
            } catch (NumberFormatException e) {
                log.warn("Could not parse VNPay amount: {}", amountStr);
            }
        }

        // VNPay response code "00" means successful transaction
        String responseCode = params.get("vnp_ResponseCode");
        boolean success = "00".equals(responseCode);

        return VnPayCallbackDto.builder()
                .success(success)
                .txnRef(txnRef)
                .orderCode(orderCode)
                .amount(amount)
                .responseCode(responseCode)
                .transactionNo(params.get("vnp_TransactionNo"))
                .bankCode(params.get("vnp_BankCode"))
                .payDate(params.get("vnp_PayDate"))
                .secureHash(params.get("vnp_SecureHash"))
                .rawParams(params)
                .build();
    }

    /**
     * HMAC-SHA512 hash using Java built-in crypto (no third-party lib needed).
     */
    private String hmacSha512(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA512 computation failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }
}
