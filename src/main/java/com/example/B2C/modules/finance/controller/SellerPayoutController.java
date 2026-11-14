package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.finance.dto.SellerPayoutDto;
import com.example.B2C.modules.finance.dto.SellerStatementDto;
import com.example.B2C.modules.finance.entity.PayoutStatus;
import com.example.B2C.modules.finance.service.PayoutService;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Payouts", description = "Seller wallet, escrow statement and payout requests")
@SecurityRequirement(name = "bearerAuth")
public class SellerPayoutController {

    private final PayoutService payoutService;
    private final SellerRepository sellerRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/wallet")
    @Operation(summary = "Get seller wallet overview: escrow, ready to payout, paid, and lifetime earnings")
    public ResponseEntity<ApiResponse<SellerStatementDto>> getWallet() {
        Long sellerId = getActiveSellerId();
        SellerStatementDto statement = payoutService.getSellerStatement(sellerId);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated payout and escrow ledger history for the authenticated seller")
    public ResponseEntity<ApiResponse<PageResponse<SellerPayoutDto>>> getHistory(
            @RequestParam(required = false) PayoutStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long sellerId = getActiveSellerId();
        Page<SellerPayoutDto> pageResult = payoutService.listPayoutsForSeller(sellerId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(pageResult)));
    }

    @PostMapping("/request-payout")
    @Operation(summary = "Seller requests payout for eligible pending balance to bank account")
    public ResponseEntity<ApiResponse<Integer>> requestPayout() {
        Long sellerId = getActiveSellerId();
        int count = payoutService.requestPayoutForSeller(sellerId);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu rút tiền thành công (" + count + " đơn đủ điều kiện)", count));
    }

    private Long getActiveSellerId() {
        Long userId = currentUserProvider.getCurrentUserId();
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Tài khoản người bán chưa được kích hoạt");
        }
        return seller.getId();
    }
}

