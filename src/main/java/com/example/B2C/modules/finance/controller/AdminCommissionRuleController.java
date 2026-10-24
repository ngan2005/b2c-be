package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.finance.dto.CommissionRuleDto;
import com.example.B2C.modules.finance.dto.CommissionRuleUpsertRequest;
import com.example.B2C.modules.finance.service.CommissionBackfillService;
import com.example.B2C.modules.finance.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/commission-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Commission Rules", description = "Admin management of platform commission rules")
@SecurityRequirement(name = "bearerAuth")
public class AdminCommissionRuleController {

    private final CommissionService commissionService;
    private final CommissionBackfillService commissionBackfillService;

    @GetMapping
    @Operation(summary = "List all commission rules, optionally filtered to active only")
    public ResponseEntity<ApiResponse<PageResponse<CommissionRuleDto>>> list(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        var result = commissionService.listRules(activeOnly, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single commission rule by ID")
    public ResponseEntity<ApiResponse<CommissionRuleDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(commissionService.getRule(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new commission rule")
    public ResponseEntity<ApiResponse<CommissionRuleDto>> create(
            @Valid @RequestBody CommissionRuleUpsertRequest request) {
        var created = commissionService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing commission rule")
    public ResponseEntity<ApiResponse<CommissionRuleDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CommissionRuleUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commissionService.updateRule(id, request)));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable a commission rule (soft-disable, does not delete)")
    public ResponseEntity<ApiResponse<Void>> disable(@PathVariable Long id) {
        commissionService.disableRule(id);
        return ResponseEntity.ok(ApiResponse.success("Commission rule disabled", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a commission rule (only allowed if no records reference it)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        commissionService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Commission rule deleted", null));
    }

    @PostMapping("/backfill")
    @Operation(summary = "Backfill commission records for all existing COMPLETED orders that are missing them")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> backfill() {
        int count = commissionBackfillService.backfillAllCompletedOrders();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("recordsCreated", count)));
    }
}
