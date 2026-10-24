package com.example.B2C.modules.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned by the seller avatar-upload endpoint.
 *
 * <p>Carries the freshly uploaded asset's public URL so the FE can swap the
 * avatar immediately without re-fetching the whole profile, plus the storage
 * {@code public_id} so the backend can destroy the replaced asset later.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Seller avatar upload response")
public class SellerLogoUploadResponse {

    @Schema(description = "Public HTTPS URL of the uploaded avatar", example = "https://res.cloudinary.com/.../seller-logo.jpg")
    private String logoUrl;

    @Schema(description = "Storage public_id of the uploaded asset")
    private String logoPublicId;

    @Schema(description = "Detected MIME type / format", example = "image/jpeg")
    private String format;

    @Schema(description = "Uploaded file size in bytes")
    private Long bytes;
}
