package com.example.B2C.modules.order.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "List my orders (filter by status, paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDto>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listOrdersForCurrentBuyer(userId, status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my orders by id")
    public ResponseEntity<ApiResponse<OrderDetailDto>> get(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderForBuyer(userId, id)));
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
}
