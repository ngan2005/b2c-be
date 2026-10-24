package com.example.B2C.modules.dispute.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.dispute.dto.DisputeDto;
import com.example.B2C.modules.dispute.dto.ResolveDisputeRequest;
import com.example.B2C.modules.dispute.entity.DisputeStatus;
import com.example.B2C.modules.dispute.service.DisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/disputes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Disputes", description = "Admin order dispute management")
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    @Operation(summary = "List disputes — optionally filter by status")
    public ResponseEntity<ApiResponse<PageResponse<DisputeDto>>> list(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                disputeService.listForAdmin(status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dispute details")
    public ResponseEntity<ApiResponse<DisputeDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(disputeService.getById(id)));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve a dispute (refund / reject / partial)")
    public ResponseEntity<ApiResponse<DisputeDto>> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveDisputeRequest req,
            @RequestHeader(value = "X-Admin-User-Id", required = false) Long adminUserId) {
        // In production, extract admin user id from the security context.
        // Here we accept it via header (the JWT already authenticated the caller).
        Long resolvedBy = resolveAdminUserId(adminUserId);
        return ResponseEntity.ok(ApiResponse.success(
                "Dispute resolved", disputeService.resolve(id, resolvedBy, req)));
    }

    private Long resolveAdminUserId(Long headerId) {
        // TODO: replace with SecurityContextHolder lookup in production
        return headerId != null ? headerId : 1L;
    }
}
