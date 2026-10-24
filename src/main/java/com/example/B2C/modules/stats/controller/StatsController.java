package com.example.B2C.modules.stats.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.stats.dto.*;
import com.example.B2C.modules.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stats", description = "Dashboard stats for seller and admin")
@SecurityRequirement(name = "bearerAuth")
public class StatsController {

    private final StatsService statsService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/api/v1/seller/stats/overview")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Aggregated KPIs for the current seller")
    public ResponseEntity<ApiResponse<SellerStatsResponse>> sellerOverview() {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(statsService.getSellerStats(userId)));
    }

    @GetMapping("/api/v1/seller/stats/revenue")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Daily revenue series for the current seller (last N days, default 30)")
    public ResponseEntity<ApiResponse<List<RevenuePoint>>> sellerRevenue(
            @RequestParam(required = false, defaultValue = "30") int days) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(statsService.getSellerRevenue(userId, days)));
    }

    @GetMapping("/api/v1/admin/stats/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aggregated KPIs across the whole platform")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> adminOverview() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getAdminStats()));
    }

    @GetMapping("/api/v1/admin/stats/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Time-series revenue for the admin dashboard")
    public ResponseEntity<ApiResponse<List<AdminRevenuePoint>>> adminRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false, defaultValue = "day") String granularity) {
        return ResponseEntity.ok(ApiResponse.success(
                statsService.getAdminRevenue(from, to, granularity)));
    }

    @GetMapping("/api/v1/admin/stats/order-funnel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Order counts grouped by status")
    public ResponseEntity<ApiResponse<List<OrderFunnelPoint>>> orderFunnel() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getOrderFunnel()));
    }

    @GetMapping("/api/v1/admin/stats/top-sellers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top sellers by revenue")
    public ResponseEntity<ApiResponse<List<TopSellerPoint>>> topSellers(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getTopSellers(limit)));
    }

    @GetMapping("/api/v1/admin/stats/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Product variants with low stock")
    public ResponseEntity<ApiResponse<List<LowStockItem>>> lowStock(
            @RequestParam(required = false, defaultValue = "10") int threshold) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getLowStock(threshold)));
    }
}