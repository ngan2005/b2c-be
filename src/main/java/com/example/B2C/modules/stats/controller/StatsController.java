package com.example.B2C.modules.stats.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.stats.dto.AdminStatsResponse;
import com.example.B2C.modules.stats.dto.RevenuePoint;
import com.example.B2C.modules.stats.dto.SellerStatsResponse;
import com.example.B2C.modules.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}