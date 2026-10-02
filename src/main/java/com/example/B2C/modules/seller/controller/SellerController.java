package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.service.SellerService;
import com.example.B2C.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller")
@RequiredArgsConstructor
@Tag(name = "Seller Profile", description = "Authenticated seller profile access")
@SecurityRequirement(name = "bearerAuth")
public class SellerController {

    private final SellerService sellerService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get my seller profile",
            description = "Returns the authenticated seller's own profile. Only accessible by SELLERs.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Seller profile not found")
    })
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getMyProfile() {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(sellerService.getMyProfile(userId)));
    }
}
