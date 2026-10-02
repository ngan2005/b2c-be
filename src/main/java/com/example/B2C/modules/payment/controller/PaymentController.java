package com.example.B2C.modules.payment.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.payment.dto.PaymentCallbackRequest;
import com.example.B2C.modules.payment.dto.PaymentDto;
import com.example.B2C.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment records and gateway callback")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
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
}
