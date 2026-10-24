package com.example.B2C.modules.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial-update payload for a seller's existing product image.
 * All fields are optional — only supplied fields are applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partial-update payload for a seller's existing product image. All fields are optional.")
public class UpdateProductImageRequest {

    @Size(max = 500, message = "altText must not exceed 500 characters")
    @Schema(description = "Replacement alt text for the image", example = "Side view of the product")
    private String altText;

    @Min(value = 0, message = "sortOrder must be >= 0")
    @Schema(description = "New sort order (0-based)", example = "2")
    private Integer sortOrder;

    @Schema(description = "Promote this image to thumbnail (true) or demote it (false)", example = "true")
    private Boolean isThumbnail;
}
