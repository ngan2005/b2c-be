package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.seller.dto.AdminSellerDto;
import com.example.B2C.modules.seller.dto.SuspendSellerRequest;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sellers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Sellers", description = "Admin management of all sellers on the platform")
@SecurityRequirement(name = "bearerAuth")
public class AdminSellerController {

    private final SellerService sellerService;

    @GetMapping
    @Operation(summary = "List all sellers platform-wide, optionally filtered by status")
    public ResponseEntity<ApiResponse<PageResponse<AdminSellerDto>>> list(
            @RequestParam(required = false) SellerStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.listSellersForAdmin(status, q, page, size)));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend a seller with a required reason")
    public ResponseEntity<ApiResponse<AdminSellerDto>> suspend(
            @PathVariable Long id,
            @Valid @RequestBody SuspendSellerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.suspendSellerForAdmin(id, request.getReason())));
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate a suspended seller")
    public ResponseEntity<ApiResponse<AdminSellerDto>> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Seller reactivated", sellerService.reactivateSellerForAdmin(id)));
    }
}
