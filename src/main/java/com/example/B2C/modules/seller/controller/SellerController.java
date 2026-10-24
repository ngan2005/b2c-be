package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.seller.dto.SellerLogoUploadResponse;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.service.SellerService;
import com.example.B2C.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/profile/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Upload the shop avatar",
            description = "Accepts a single multipart file (image/jpeg, image/png, image/webp). "
                    + "The asset is streamed to the storage backend (Cloudinary in production) and "
                    + "seller.logo_url is updated to point at it. The previously stored avatar is "
                    + "destroyed from the storage backend so it does not linger as an orphan.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avatar uploaded",
                    content = @Content(schema = @Schema(implementation = SellerLogoUploadResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Empty file or unsupported image type"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Seller profile not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Storage backend not configured")
    })
    public ResponseEntity<ApiResponse<SellerLogoUploadResponse>> uploadLogo(
            @Parameter(description = "Image file (max 5 MB, allowed: JPEG/PNG/WebP)")
            @RequestParam("file") MultipartFile file) {

        Long userId = currentUserProvider.getCurrentUserId();
        SellerLogoUploadResponse response = sellerService.uploadLogo(userId, file);
        return ResponseEntity.ok(ApiResponse.success("Avatar updated", response));
    }
}
