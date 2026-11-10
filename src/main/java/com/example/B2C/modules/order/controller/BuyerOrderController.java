package com.example.B2C.modules.order.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderPreviewResponse;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.dto.PreviewOrderRequest;
import com.example.B2C.modules.order.dto.ReturnRequestDto;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/orders")
@RequiredArgsConstructor
@Tag(name = "Buyer Orders", description = "Checkout and order management for the authenticated buyer")
@SecurityRequirement(name = "bearerAuth")
public class BuyerOrderController {

    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    // P2.1 — MUST be declared BEFORE /{id} to avoid path conflict
    @PostMapping("/preview")
    @Operation(summary = "Preview order total — subtotal, shipping fee, discount (no DB write)")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> preview(
            @RequestBody PreviewOrderRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        OrderPreviewResponse dto = orderService.previewOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    @Operation(summary = "Checkout — create a new order from the cart")
    public ResponseEntity<ApiResponse<OrderDetailDto>> create(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = currentUserProvider.getCurrentUserId();
        OrderDetailDto dto = orderService.createOrderForBuyer(userId, request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
    }

    @GetMapping
    @Operation(summary = "List my orders (filter by status, date, amount, payment method, paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDto>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime toDate,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listOrdersForCurrentBuyer(
                        userId, status, search, paymentMethod, fromDate, toDate, minAmount, maxAmount, sortBy, sortDir, page, size)));
    }

    /**
     * Read-only order history — accessible to BUYER and SELLER.
     */
    @GetMapping("/history")
    @Operation(summary = "Read-only order history — accessible to BUYER and SELLER")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDto>>> history(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listHistoricalBuyerOrders(userId, status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my orders by id")
    public ResponseEntity<ApiResponse<OrderDetailDto>> get(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderForBuyer(userId, id)));
    }

    // P1.2 — Retry payment for unpaid PENDING order
    @PostMapping("/{id}/pay")
    @Operation(summary = "Retry VNPay payment for a PENDING + UNPAID order",
               description = "Returns a VNPay redirect URL. Only valid if order status is PENDING and paymentStatus is UNPAID.")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> retryPay(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = currentUserProvider.getCurrentUserId();
        String clientIp = httpRequest.getRemoteAddr();
        String paymentUrl = orderService.retryPaymentForBuyer(userId, id, clientIp);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("paymentUrl", paymentUrl)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel my order (only allowed for PENDING or CONFIRMED orders)")
    public ResponseEntity<ApiResponse<OrderDetailDto>> cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrderForBuyer(userId, id, reason)));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Confirm order received and complete the order")
    public ResponseEntity<ApiResponse<OrderDetailDto>> complete(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Order completed",
                orderService.completeOrderForBuyer(userId, id)));
    }

    @PostMapping("/{id}/return-request")
    @Operation(summary = "Request a return for a delivered order (within 7 days of delivery)")
    public ResponseEntity<ApiResponse<OrderDetailDto>> requestReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnRequestDto request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Return requested",
                orderService.requestReturn(userId, id, request)));
    }
}
