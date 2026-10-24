package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.catalog.dto.ProductImageDto;
import com.example.B2C.modules.catalog.dto.ProductImageUploadResponse;
import com.example.B2C.modules.catalog.dto.UpdateProductImageRequest;
import com.example.B2C.modules.catalog.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Seller endpoints for managing product images.
 * <p>
 * Uploads are streamed to the storage backend (Cloudinary in production,
 * swappable via {@code app.storage.provider}). The response carries the
 * public URL and the storage public_id so the FE can render thumbnails
 * immediately and the seller can issue targeted deletes.
 */
@RestController
@RequestMapping("/api/v1/seller/products/{productId}/images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Seller Product Images",
        description = "Upload and delete product images. Backend streams to Cloudinary.")
@SecurityRequirement(name = "bearerAuth")
public class SellerProductImageController {

    private final ProductImageService productImageService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload one product image",
            description = "Accepts a single multipart file (image/jpeg, image/png, image/webp, ≤ 5 MB). "
                    + "Returns the persisted product_image row including the Cloudinary URL and public_id.")
    public ResponseEntity<ApiResponse<ProductImageUploadResponse>> upload(
            @PathVariable Long productId,
            @Parameter(description = "Image file (max 5 MB, allowed: JPEG/PNG/WebP)")
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "isThumbnail", required = false, defaultValue = "false") Boolean isThumbnail) {

        Long userId = currentUserProvider.getCurrentUserId();
        ProductImageUploadResponse response = productImageService.uploadForSeller(
                userId, productId, file, altText, isThumbnail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete one product image (soft-delete)",
            description = "Marks the image as deleted so the seller UI can offer an undo toast. "
                    + "The Cloudinary asset is preserved until a cleanup job removes it after the undo window expires.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        Long userId = currentUserProvider.getCurrentUserId();
        productImageService.deleteForSeller(userId, productId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted", null));
    }

    @PostMapping("/{imageId}/restore")
    @Operation(summary = "Restore a previously soft-deleted image within the undo window")
    public ResponseEntity<ApiResponse<ProductImageDto>> restore(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        Long userId = currentUserProvider.getCurrentUserId();
        ProductImageDto dto = productImageService.restoreForSeller(userId, productId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image restored", dto));
    }

    @PatchMapping("/{imageId}")
    @Operation(summary = "Update image metadata",
            description = "Partially update an existing product image — alt text, sort order, or thumbnail flag. " +
                    "No file re-upload is performed.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Image updated",
                    content = @Content(schema = @Schema(implementation = ProductImageDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a seller or seller not active"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or image not found")
    })
    public ResponseEntity<ApiResponse<ProductImageDto>> update(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @Valid @RequestBody UpdateProductImageRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();
        ProductImageDto dto = productImageService.updateForSeller(userId, productId, imageId, request);
        return ResponseEntity.ok(ApiResponse.success("Image updated", dto));
    }
}
