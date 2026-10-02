package com.example.B2C.modules.order.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.dto.UpdateOrderStatusRequest;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Orders", description = "Order management for the authenticated seller")
@SecurityRequirement(name = "bearerAuth")
public class SellerOrderController {

    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "List my orders (filter by status, paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDto>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(orderService.listOrdersForCurrentSeller(userId, status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my orders by id")
    public ResponseEntity<ApiResponse<OrderDetailDto>> get(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderForSeller(userId, id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status",
            description = "Transitions are validated server-side. PENDING -> CONFIRMED -> PACKED -> SHIPPING -> DELIVERED -> COMPLETED. CANCELLED is allowed up to PACKED.")
    public ResponseEntity<ApiResponse<OrderDetailDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        OrderStatus status = OrderStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                orderService.updateOrderStatusForSeller(userId, id, status, request.getNote())));
    }
}