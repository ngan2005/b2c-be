package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.finance.dto.SellerPayoutDto;
import com.example.B2C.modules.finance.dto.SellerStatementDto;
import com.example.B2C.modules.finance.entity.PayoutStatus;
import com.example.B2C.modules.finance.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for managing seller payouts.
 *
 * <p>Base path: /api/v1/admin/payouts
 */
@RestController
@RequestMapping("/api/v1/admin/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Payouts", description = "Seller payout management for admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminPayoutController {

    private final PayoutService payoutService;

    @GetMapping
    @Operation(summary = "List all payouts, optionally filtered by status",
               description = "Results are ordered by createdAt descending (most recent first).")
    public ResponseEntity<ApiResponse<PageResponse<SellerPayoutDto>>> list(
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Page<SellerPayoutDto> pageResult = payoutService.listPayouts(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(pageResult)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single payout record by its ID")
    public ResponseEntity<ApiResponse<SellerPayoutDto>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payoutService.getPayout(id)));
    }

    @GetMapping("/seller/{sellerId}/statement")
    @Operation(summary = "Get a seller's payout statement",
               description = "Returns pending/paid/lifetime totals and recent PAID payouts for the seller.")
    public ResponseEntity<ApiResponse<SellerStatementDto>> getStatement(@PathVariable Long sellerId) {
        return ResponseEntity.ok(ApiResponse.success(payoutService.getSellerStatement(sellerId)));
    }

    @PostMapping("/{id}/mark-paid")
    @Operation(summary = "Mark a payout as paid and record the bank transfer reference",
               description = "Debits the platform wallet by the net payout amount. "
                           + "Can only be called once per payout (idempotent re-calls return 409).")
    public ResponseEntity<ApiResponse<SellerPayoutDto>> markPaid(
            @PathVariable Long id,
            @Valid @RequestBody com.example.B2C.modules.finance.dto.MarkPaidRequest request) {

        // Admin user resolved from security context via the caller (admin service / interceptor)
        Long adminUserId = resolveCurrentAdminUserId();
        SellerPayoutDto updated = payoutService.markPaid(id, adminUserId, request.getBankReference());
        return ResponseEntity.ok(ApiResponse.success("Payout marked as paid", updated));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a PENDING payout",
               description = "Only payouts in PENDING status can be cancelled.")
    public ResponseEntity<ApiResponse<SellerPayoutDto>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payout cancelled", payoutService.cancelPayout(id)));
    }

    // -------------------------------------------------------------------------
    // The admin user id is resolved from the security context the same way
    // existing controllers do it (via CurrentUserProvider).
    // -------------------------------------------------------------------------
    private Long resolveCurrentAdminUserId() {
        var provider = new com.example.B2C.common.security.CurrentUserProvider();
        return provider.getCurrentUserId();
    }
}
