package com.example.B2C.modules.catalog.api;

import com.example.B2C.modules.catalog.api.dto.ProductVariantView;

import java.math.BigDecimal;

/**
 * Facade exposing a narrow, read-only slice of the catalog module to other modules
 * (order, cart, payment, etc.).  Callers must NOT access
 * {@code com.example.B2C.modules.catalog.entity.*} or
 * {@code com.example.B2C.modules.catalog.repository.*} directly.
 */
public interface CatalogFacade {

    /**
     * Full read-only view of a variant by id.
     *
     * @throws com.example.B2C.common.exception.ResourceNotFoundException if variant not found
     */
    ProductVariantView getVariant(Long variantId);

    /** @return the current unit price of the variant, or null if not found. */
    BigDecimal getUnitPrice(Long variantId);

    /** @return the product id that owns this variant, or null if not found. */
    Long getProductIdForVariant(Long variantId);

    /** @return the seller id that owns this variant, or null if not found. */
    Long getSellerIdForVariant(Long variantId);

    /** @return the variant display name, or null if not found. */
    String getVariantName(Long variantId);

    /** @return the main image URL of the variant, or null if not found. */
    String getImageUrl(Long variantId);
}
