package com.example.B2C.modules.order.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Orders", description = "Admin order management")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List all orders (filterable by status, paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDto>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        // Admin sees all orders — pass null as userId to bypass buyer ownership check.
        // We route through a new service method for admins.
        return ResponseEntity.ok(ApiResponse.success(
                orderService.listOrdersForAdmin(status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail by id (admin view)")
    public ResponseEntity<ApiResponse<OrderDetailDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderForAdmin(id)));
    }

    @PostMapping("/{id}/approve-return")
    @Operation(summary = "Approve a return request for an order")
    public ResponseEntity<ApiResponse<OrderDetailDto>> approveReturn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Return approved",
                orderService.approveReturn(id)));
    }

    @PostMapping("/{id}/reject-return")
    @Operation(summary = "Reject a return request for an order")
    public ResponseEntity<ApiResponse<OrderDetailDto>> rejectReturn(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String note = (body != null) ? body.get("adminNote") : null;
        return ResponseEntity.ok(ApiResponse.success("Return rejected",
                orderService.rejectReturn(id, note)));
    }
}
