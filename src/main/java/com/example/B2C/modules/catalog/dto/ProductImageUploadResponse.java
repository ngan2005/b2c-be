package com.example.B2C.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload returned by the seller product-image upload endpoint.
 * <p>
 * Mirrors {@link ProductImageDto} but additionally exposes
 * {@link #cloudinaryPublicId} so the seller UI can issue targeted deletes
 * without re-fetching the entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadResponse {

    private Long id;
    private Long productId;
    private String imageUrl;
    private String cloudinaryPublicId;
    private String altText;
    private Integer sortOrder;
    private Boolean isThumbnail;
    private Long bytes;
    private String format;
    private LocalDateTime createdAt;
}
