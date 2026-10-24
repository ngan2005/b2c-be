package com.example.B2C.modules.promotion.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.promotion.dto.ApplyPromotionRequest;
import com.example.B2C.modules.promotion.dto.ApplyPromotionResponse;
import com.example.B2C.modules.promotion.dto.CreatePromotionRequest;
import com.example.B2C.modules.promotion.dto.PromotionDto;
import com.example.B2C.modules.promotion.dto.UpdatePromotionRequest;
import com.example.B2C.modules.promotion.entity.PromotionStatus;
import com.example.B2C.modules.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Promotions", description = "Promotion and coupon management for sellers, and buyer redemption")
public class PromotionController {

    private final PromotionService promotionService;
    private final CurrentUserProvider currentUserProvider;

    // ==================== PUBLIC / BUYER endpoints ====================

    @GetMapping("/promotions")
    @Operation(
            summary = "List active public promotions for visitors/buyers",
            description = "Returns platform-wide and shop promotions currently active for all visitors."
    )
    public ResponseEntity<ApiResponse<List<PromotionDto>>> listPublicPromotions() {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.listActivePromotionsForBuyer(null)));
    }

    @GetMapping("/buyer/promotions")
    @Operation(
            summary = "List active promotions available to the buyer",
            description = "Returns platform-wide and seller-specific promotions that are currently active, "
                    + "have not expired, and have remaining quantity."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotions retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<List<PromotionDto>>> listActivePromotions() {
        Long buyerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.listActivePromotionsForBuyer(buyerId)));
    }

    @PostMapping("/buyer/promotions/validate")
    @Operation(
            summary = "Validate a promotion code before checkout",
            description = "Checks whether a promotion code is valid for the given order subtotal "
                    + "and returns the computed discount amount. Does NOT consume the promotion."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation result returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<ApplyPromotionResponse>> validatePromotion(
            @Valid @RequestBody ApplyPromotionRequest request) {
        Long buyerId = currentUserProvider.getCurrentUserId();
        ApplyPromotionResponse result = promotionService.validatePromotion(buyerId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== SELLER endpoints ====================

    @PostMapping("/seller/promotions")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Create a new promotion/coupon",
            description = "Seller creates a new promotion. Codes are unique and auto-uppercased."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Promotion created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller")
    })
    public ResponseEntity<ApiResponse<PromotionDto>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        PromotionDto dto = promotionService.createPromotion(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
    }

    @GetMapping("/seller/promotions")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "List promotions owned by the authenticated seller")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotions retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller")
    })
    public ResponseEntity<ApiResponse<PageResponse<PromotionDto>>> listMyPromotions(
            @Parameter(description = "Filter by status") @RequestParam(required = false) PromotionStatus status,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.listForCurrentSeller(userId, status, page, size)));
    }

    @GetMapping("/seller/promotions/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get one promotion by ID (seller must own it)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotion retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<PromotionDto>> getPromotion(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.getPromotion(userId, id)));
    }

    @PutMapping("/seller/promotions/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update a promotion owned by the current seller",
            description = "Full update — every editable field is replaced by the request body. "
                    + "Redemption counters and status are owned by the platform and cannot be changed here.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotion updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Code already exists")
    })
    public ResponseEntity<ApiResponse<PromotionDto>> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Promotion updated",
                promotionService.updatePromotion(userId, id, request)));
    }

    @PatchMapping("/seller/promotions/{id}/disable")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Disable a promotion",
            description = "Immediately marks the promotion as DISABLED, preventing further redemptions. "
                    + "Already-used counts are preserved."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotion disabled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<PromotionDto>> disablePromotion(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Promotion disabled",
                promotionService.disablePromotion(userId, id)));
    }
}
