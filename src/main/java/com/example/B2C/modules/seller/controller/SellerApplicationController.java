package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.seller.dto.CreateSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationResponse;
import com.example.B2C.modules.seller.service.SellerApplicationService;
import com.example.B2C.common.security.CurrentUserProvider;
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
@RequestMapping("/api/v1/seller-applications")
@RequiredArgsConstructor
@Tag(name = "Seller Applications (Buyer)", description = "Submit and track seller applications")
@SecurityRequirement(name = "bearerAuth")
public class SellerApplicationController {

    private final SellerApplicationService applicationService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Submit a seller application",
            description = "A BUYER submits a request to become a seller. Creates a PENDING application and a PENDING seller profile.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Application submitted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or user lacks BUYER role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already a seller or pending application exists")
    })
    public ResponseEntity<ApiResponse<SellerApplicationResponse>> submit(
            @Valid @RequestBody CreateSellerApplicationRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        SellerApplicationResponse response = applicationService.submit(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }
}
