package com.example.B2C.modules.catalog.api;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.catalog.api.dto.ProductVariantView;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of {@link CatalogFacade}.  Lives in the {@code api} package so
 * that ArchUnit rules can distinguish "public facade" from "internal service".
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CatalogFacadeImpl implements CatalogFacade {

    private final ProductVariantRepository variantRepository;

    @Override
    public ProductVariantView getVariant(Long variantId) {
        ProductVariant v = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id: " + variantId));
        return toView(v);
    }

    @Override
    public BigDecimal getUnitPrice(Long variantId) {
        return variantRepository.findById(variantId)
                .map(v -> {
                    BigDecimal sale = v.getSalePrice();
                    return (sale != null) ? sale : v.getPrice();
                })
                .orElse(null);
    }

    @Override
    public Long getProductIdForVariant(Long variantId) {
        return variantRepository.findById(variantId)
                .map(v -> v.getProduct().getId())
                .orElse(null);
    }

    @Override
    public Long getSellerIdForVariant(Long variantId) {
        return variantRepository.findById(variantId)
                .map(v -> v.getProduct().getSeller().getId())
                .orElse(null);
    }

    @Override
    public String getVariantName(Long variantId) {
        return variantRepository.findById(variantId)
                .map(ProductVariant::getVariantName)
                .orElse(null);
    }

    @Override
    public String getImageUrl(Long variantId) {
        return variantRepository.findById(variantId)
                .map(this::resolveImageUrl)
                .orElse(null);
    }

    private String resolveImageUrl(ProductVariant v) {
        if (v.getImageUrl() != null && !v.getImageUrl().isBlank()) {
            return v.getImageUrl();
        }
        if (v.getProduct() != null && v.getProduct().getThumbnailUrl() != null && !v.getProduct().getThumbnailUrl().isBlank()) {
            return v.getProduct().getThumbnailUrl();
        }
        return null;
    }

    private ProductVariantView toView(ProductVariant v) {
        BigDecimal price = v.getSalePrice() != null ? v.getSalePrice() : v.getPrice();
        int reserved = v.getReservedQuantity() != null ? v.getReservedQuantity() : 0;
        int available = Math.max(0, v.getStockQuantity() - reserved);
        return new ProductVariantView(
                v.getId(),
                v.getProduct().getId(),
                v.getProduct().getName(),
                v.getProduct().getSeller().getId(),
                v.getVariantName(),
                resolveImageUrl(v),
                price,
                available
        );
    }
}
