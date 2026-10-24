package com.example.B2C.modules.promotion.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.promotion.dto.CreatePromotionRequest;
import com.example.B2C.modules.promotion.dto.PromotionDto;
import com.example.B2C.modules.promotion.dto.UpdatePromotionRequest;
import com.example.B2C.modules.promotion.entity.PromotionStatus;
import com.example.B2C.modules.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Promotions", description = "Admin management of all platform promotions")
@SecurityRequirement(name = "bearerAuth")
public class AdminPromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @Operation(summary = "Create a platform-wide promotion/coupon",
            description = "Admin creates a promotion that is not owned by any shop (seller_id is NULL). "
                    + "Codes are unique and auto-uppercased.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Promotion created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not an admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Code already exists")
    })
    public ResponseEntity<ApiResponse<PromotionDto>> create(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionDto dto = promotionService.createForAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
    }

    @GetMapping
    @Operation(summary = "List all promotions platform-wide, optionally filtered by status")
    public ResponseEntity<ApiResponse<PageResponse<PromotionDto>>> list(
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.listAllForAdmin(status, page, size)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update any promotion (platform-wide or seller-owned)",
            description = "Full update — every editable field is replaced by the request body. "
                    + "Redemption counters and status are owned by the platform and cannot be changed here.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Promotion updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not an admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Code already exists")
    })
    public ResponseEntity<ApiResponse<PromotionDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promotion updated",
                promotionService.updateForAdmin(id, request)));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable a promotion (sets status to DISABLED)")
    public ResponseEntity<ApiResponse<PromotionDto>> disable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Promotion disabled", promotionService.disableForAdmin(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a promotion (can be restored via the undo toast)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        promotionService.deleteForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion deleted", null));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore a previously soft-deleted promotion within the undo window")
    public ResponseEntity<ApiResponse<PromotionDto>> restore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Promotion restored",
                promotionService.restoreForAdmin(id)));
    }
}
