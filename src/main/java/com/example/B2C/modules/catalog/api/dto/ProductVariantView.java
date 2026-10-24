package com.example.B2C.modules.catalog.api.dto;

import java.math.BigDecimal;

/**
 * Read-only view of a product variant, exposed to other modules via CatalogFacade.
 * Only contains the fields that external modules (order, cart, payment, etc.)
 * are permitted to access — no internal JPA entity details.
 */
public record ProductVariantView(
        Long id,
        Long productId,
        String productName,
        Long sellerId,
        String variantName,
        String imageUrl,
        BigDecimal unitPrice,
        Integer availableStock
) {}
