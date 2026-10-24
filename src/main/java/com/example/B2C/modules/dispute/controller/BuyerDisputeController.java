package com.example.B2C.modules.dispute.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.dispute.dto.DisputeDto;
import com.example.B2C.modules.dispute.dto.OpenDisputeRequest;
import com.example.B2C.modules.dispute.service.DisputeService;
import com.example.B2C.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/orders/{orderId}/dispute")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Buyer Disputes", description = "Buyer dispute management")
public class BuyerDisputeController {

    private final DisputeService disputeService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @Operation(summary = "Open a dispute on one of my orders")
    public ResponseEntity<ApiResponse<DisputeDto>> open(
            @PathVariable Long orderId,
            @Valid @RequestBody OpenDisputeRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        DisputeDto result = disputeService.openDispute(orderId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result));
    }
}
