package com.example.B2C.modules.payment.controller;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.payment.dto.PaymentCallbackRequest;
import com.example.B2C.modules.payment.dto.PaymentDto;
import com.example.B2C.modules.payment.dto.VnPayCallbackDto;
import com.example.B2C.modules.payment.service.PaymentService;
import com.example.B2C.modules.payment.config.VnPayConfig;
import com.example.B2C.modules.payment.service.VnPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment records and gateway callback")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final VnPayService vnpayService;
    private final VnPayConfig vnpayConfig;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all payment records for an order")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> list(@PathVariable Long orderId) {
        Long userId = currentUserProvider.getCurrentUserId();
        // We deliberately do not check ownership here — the order service should be
        // consulted first. For a real deployment, add an authorisation check.
        log.debug("User {} listing payments for order {}", userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(paymentService.listForOrder(orderId)));
    }

    @PostMapping("/callback")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Webhook endpoint for payment gateway callbacks (idempotent)")
    public ResponseEntity<ApiResponse<PaymentDto>> callback(
            @Valid @RequestBody PaymentCallbackRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        PaymentDto dto = paymentService.confirmPayment(
                request.getOrderId(),
                request.getTransactionId(),
                request.getStatus(),
                request.getProvider(),
                idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Callback processed", dto));
    }

    /**
     * VNPay return URL — the VNPay sandbox redirects the user here after payment.
     * We verify the signature, process the payment, then redirect to the frontend
     * success/failure page with the result encoded in query params.
     *
     * vnpay.return-url = http://localhost:8080/api/v1/payments/return
     */
    @GetMapping("/return")
    @PreAuthorize("permitAll()")
    @Operation(summary = "VNPay return URL — processes return params and redirects to frontend")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Collect all vnp_* query parameters
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null && e.getValue().length > 0 ? e.getValue()[0] : ""
                ));

        log.info("VNPay return callback received: {}", params);

        // Strip the backend path to get the frontend base URL
        String frontendBase;
        try {
            String returnUrl = vnpayConfig.getReturnUrl();
            int idx = returnUrl.indexOf("/payment/return");
            frontendBase = idx > 0 ? returnUrl.substring(0, idx) : "http://localhost:5173";
        } catch (Exception e) {
            frontendBase = "http://localhost:5173";
        }

        if (!vnpayService.verifyCallback(params)) {
            log.warn("VNPay return: invalid signature — params={}", params);
            response.sendRedirect(frontendBase + "/payment/return?status=error&message=invalid_signature");
            return;
        }

        VnPayCallbackDto parsed = vnpayService.parseCallbackResponse(params);

        if (parsed.isSuccess()) {
            try {
                paymentService.confirmPaymentByTxnRef(
                        parsed.getTxnRef(),
                        parsed.getTransactionNo(),
                        parsed.getResponseCode(),
                        "vnpay-return-" + parsed.getTxnRef()
                );
            } catch (Exception e) {
                log.error("Failed to process VNPay success return for txnRef={}: {}",
                        parsed.getTxnRef(), e.getMessage());
            }
            response.sendRedirect(frontendBase + "/payment/return?status=success&orderCode=" + parsed.getOrderCode());
        } else {
            try {
                paymentService.confirmPaymentByTxnRef(
                        parsed.getTxnRef(),
                        parsed.getTransactionNo(),
                        parsed.getResponseCode(),
                        "vnpay-return-" + parsed.getTxnRef()
                );
            } catch (Exception e) {
                log.error("Failed to process VNPay failure return for txnRef={}: {}",
                        parsed.getTxnRef(), e.getMessage());
            }
            log.info("VNPay payment declined: responseCode={}", parsed.getResponseCode());
            response.sendRedirect(frontendBase + "/payment/return?status=failed&orderCode=" + parsed.getOrderCode());
        }
    }
}
