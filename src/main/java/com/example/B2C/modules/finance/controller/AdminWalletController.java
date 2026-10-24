package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.finance.dto.AdjustmentRequest;
import com.example.B2C.modules.finance.dto.PlatformWalletDto;
import com.example.B2C.modules.finance.entity.TxnDirection;
import com.example.B2C.modules.finance.service.PlatformWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for viewing and adjusting the platform wallet.
 *
 * <p>Base path: /api/v1/admin/wallet
 */
@RestController
@RequestMapping("/api/v1/admin/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Wallet", description = "Platform wallet view and manual adjustments")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AdminWalletController {

    private final PlatformWalletService walletService;

    @GetMapping
    @Operation(summary = "Get the current platform wallet balance and recent transactions")
    public ResponseEntity<ApiResponse<PlatformWalletDto>> getWallet() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWallet()));
    }

    @PostMapping("/adjustments")
    @Operation(summary = "Apply a manual balance adjustment to the platform wallet",
               description = "Use CREDIT to add funds (e.g. platform promo budget) "
                           + "and DEBIT to subtract funds (e.g. correcting an over-credit). "
                           + "Both directions write an immutable transaction row.")
    public ResponseEntity<ApiResponse<PlatformWalletDto>> adjust(
            @Valid @RequestBody AdjustmentRequest request) {

        Long adminUserId = resolveCurrentAdminUserId();
        String reason = request.getReason() != null ? request.getReason() : "MANUAL_ADJUSTMENT";
        String note = (request.getNote() != null ? request.getNote() : "")
                + " [by admin " + adminUserId + "]";

        try {
            if (request.getDirection() == TxnDirection.CREDIT) {
                walletService.credit(request.getAmount(), reason, null, null, note);
            } else {
                walletService.debit(request.getAmount(), reason, null, null, note);
            }
        } catch (IllegalStateException ex) {
            // Thrown by debit() when balance is insufficient
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("Adjustment failed: " + ex.getMessage()));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Wallet " + request.getDirection() + " of " + request.getAmount() + " applied",
                walletService.getWallet()));
    }

    private Long resolveCurrentAdminUserId() {
        var provider = new com.example.B2C.common.security.CurrentUserProvider();
        return provider.getCurrentUserId();
    }
}
