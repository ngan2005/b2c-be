package com.example.B2C.modules.inventory.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.inventory.dto.SellerInventoryItemDto;
import com.example.B2C.modules.inventory.dto.SellerInventoryOverviewDto;
import com.example.B2C.modules.inventory.dto.UpdateStockRequest;
import com.example.B2C.modules.inventory.service.SellerInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Inventory", description = "Endpoints for seller stock and warehouse management")
@SecurityRequirement(name = "bearerAuth")
public class SellerInventoryController {

    private final SellerInventoryService sellerInventoryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "Get seller inventory hub overview and variants list")
    public ResponseEntity<ApiResponse<SellerInventoryOverviewDto>> getInventory(
            @RequestParam(required = false, defaultValue = "ALL") String filter,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        SellerInventoryOverviewDto dto = sellerInventoryService.getInventoryOverview(userId, filter, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{variantId}/stock")
    @Operation(summary = "Update on-hand stock quantity for a variant")
    public ResponseEntity<ApiResponse<SellerInventoryItemDto>> updateStock(
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateStockRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        SellerInventoryItemDto updated = sellerInventoryService.updateVariantStock(userId, variantId, request.getStockQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tồn kho thành công", updated));
    }
}
