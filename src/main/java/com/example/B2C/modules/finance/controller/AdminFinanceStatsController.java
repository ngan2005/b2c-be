package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.finance.dto.DailyRevenuePointDto;
import com.example.B2C.modules.finance.dto.FinanceOverviewDto;
import com.example.B2C.modules.finance.service.FinanceStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only endpoints for the finance dashboard.
 */
@RestController
@RequestMapping("/api/v1/admin/stats/finance")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Finance Stats", description = "Finance KPIs and revenue series for the admin dashboard")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminFinanceStatsController {

    private final FinanceStatsService financeStatsService;

    @GetMapping
    @Operation(summary = "Finance overview KPIs", description = "Returns GMV, platform revenue, payout totals, order counts, and top-5 categories.")
    public ResponseEntity<ApiResponse<FinanceOverviewDto>> overview() {
        return ResponseEntity.ok(ApiResponse.success(financeStatsService.getOverview()));
    }

    @GetMapping("/daily")
    @Operation(summary = "Daily revenue series", description = "Returns per-day GMV, platform revenue, and seller net for the last N days (default 30).")
    public ResponseEntity<ApiResponse<List<DailyRevenuePointDto>>> daily(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(financeStatsService.getDailySeries(days)));
    }
}
