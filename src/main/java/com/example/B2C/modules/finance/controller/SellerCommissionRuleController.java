package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.finance.dto.CommissionRuleDto;
import com.example.B2C.modules.finance.entity.CommissionRule;
import com.example.B2C.modules.finance.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller/commission-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Commission Rules", description = "Seller view of platform commission rules and simulation")
@SecurityRequirement(name = "bearerAuth")
public class SellerCommissionRuleController {

    private final CommissionService commissionService;

    @GetMapping
    @Operation(summary = "List active platform commission rules for sellers")
    public ResponseEntity<ApiResponse<PageResponse<CommissionRuleDto>>> list(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {
        var result = commissionService.listRules(true, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/simulate")
    @Operation(summary = "Simulate commission fee for a given category and price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulate(
            @RequestParam(required = false) Long categoryId,
            @RequestParam BigDecimal amount) {
        CommissionRule rule = commissionService.resolveRule(categoryId);
        BigDecimal orderAmount = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal rate = rule.getPercent();

        BigDecimal minOrderValue = rule.getMinOrderValue() != null ? rule.getMinOrderValue() : BigDecimal.ZERO;
        boolean belowMin = minOrderValue.compareTo(BigDecimal.ZERO) > 0 && orderAmount.compareTo(minOrderValue) < 0;

        BigDecimal fee;
        if (belowMin) {
            fee = BigDecimal.ZERO;
        } else {
            fee = orderAmount.multiply(rate).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            BigDecimal maxFee = rule.getMaxCommission();
            if (maxFee != null && fee.compareTo(maxFee) > 0) {
                fee = maxFee;
            }
        }

        BigDecimal netReceived = orderAmount.subtract(fee);

        Map<String, Object> resp = new HashMap<>();
        resp.put("ruleId", rule.getId());
        resp.put("ruleName", rule.getName());
        resp.put("percent", rate);
        resp.put("orderAmount", orderAmount);
        resp.put("commissionFee", fee);
        resp.put("sellerNetReceived", netReceived);
        resp.put("minOrderValue", rule.getMinOrderValue());
        resp.put("maxCommission", rule.getMaxCommission());
        resp.put("isBelowMinOrder", belowMin);

        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}
