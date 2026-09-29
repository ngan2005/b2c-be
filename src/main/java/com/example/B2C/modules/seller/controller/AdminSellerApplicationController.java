package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.seller.dto.RejectSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.service.SellerApplicationService;
import com.example.B2C.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/seller-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Seller Applications (Admin)", description = "Admin review and management of seller applications")
@SecurityRequirement(name = "bearerAuth")
public class AdminSellerApplicationController {

    private final SellerApplicationService applicationService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "List all seller applications",
            description = "Admin lists all seller applications, optionally filtered by status. Results are paginated.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Applications retrieved")
    })
    public ResponseEntity<ApiResponse<PageResponse<SellerApplicationAdminDetailResponse>>> list(
            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) SellerApplicationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(applicationService.list(status, pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get seller application detail",
            description = "Admin views full details of a single application.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Application found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<ApiResponse<SellerApplicationAdminDetailResponse>> getDetail(
            @Parameter(description = "Application ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getDetail(id)));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a seller application",
            description = "Admin approves a PENDING application. Grants SELLER role, activates seller profile.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Application approved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Application already approved/rejected")
    })
    public ResponseEntity<ApiResponse<SellerApplicationAdminDetailResponse>> approve(
            @Parameter(description = "Application ID") @PathVariable Long id) {
        Long adminId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(applicationService.approve(adminId, id)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a seller application",
            description = "Admin rejects a PENDING application with a required reason.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Application rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Rejection reason is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Application already rejected/approved")
    })
    public ResponseEntity<ApiResponse<SellerApplicationAdminDetailResponse>> reject(
            @Parameter(description = "Application ID") @PathVariable Long id,
            @Valid @RequestBody RejectSellerApplicationRequest request) {
        Long adminId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(applicationService.reject(adminId, id, request)));
    }
}
