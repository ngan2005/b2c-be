package com.example.B2C.modules.catalog.api;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.catalog.api.dto.ProductVariantView;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
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

    @Override
    public Integer getWeightGram(Long variantId) {
        return variantRepository.findById(variantId)
                .map(v -> {
                    if (v.getWeightGram() != null && v.getWeightGram() > 0) {
                        return v.getWeightGram();
                    }
                    if (v.getProduct() != null && v.getProduct().getWeightGram() != null && v.getProduct().getWeightGram() > 0) {
                        return v.getProduct().getWeightGram();
                    }
                    return 250; // default 250g
                })
                .orElse(250);
    }

    @Override
    public ProductVariantView validateVariantForPurchase(
            Long variantId,
            String fallbackProductName,
            String fallbackVariantName,
            int requestedQuantity) {
        String defaultName = (fallbackProductName != null && !fallbackProductName.isBlank())
                ? fallbackProductName
                : "Sản phẩm";

        if (variantId == null) {
            throw new BadRequestException("Sản phẩm \"" + defaultName + "\" không còn tồn tại, không thể mua lại.");
        }

        ProductVariant v = variantRepository.findById(variantId).orElse(null);
        if (v == null) {
            throw new BadRequestException("Sản phẩm \"" + defaultName + "\" không còn tồn tại trên hệ thống, không thể mua lại.");
        }

        Product p = v.getProduct();
        String productName = (p != null && p.getName() != null && !p.getName().isBlank())
                ? p.getName()
                : defaultName;
        String varName = (v.getVariantName() != null && !v.getVariantName().isBlank())
                ? v.getVariantName()
                : fallbackVariantName;
        String varLabel = (varName != null && !varName.isBlank()) ? " (" + varName + ")" : "";

        if (p == null || p.getDeletedAt() != null) {
            throw new BadRequestException("Sản phẩm \"" + productName + "\" đã ngừng kinh doanh hoặc bị xóa, không cho phép bán.");
        }

        if (p.getStatus() != ProductStatus.ACTIVE) {
            if (p.getStatus() == ProductStatus.BANNED) {
                throw new BadRequestException("Sản phẩm \"" + productName + "\" đã bị khóa kinh doanh, không cho phép bán.");
            }
            throw new BadRequestException("Sản phẩm \"" + productName + "\" hiện đã ngừng bán, không cho phép mua lại.");
        }

        Seller seller = p.getSeller();
        if (seller == null || seller.getStatus() != SellerStatus.ACTIVE) {
            throw new BadRequestException("Gian hàng của sản phẩm \"" + productName + "\" hiện đang tạm ngưng hoạt động, không cho phép bán.");
        }

        if (!Boolean.TRUE.equals(v.getIsActive())) {
            throw new BadRequestException("Phân loại" + varLabel + " của sản phẩm \"" + productName + "\" hiện đã ngừng bán, không cho phép mua lại.");
        }

        int stock = v.getStockQuantity() != null ? v.getStockQuantity() : 0;
        int reserved = v.getReservedQuantity() != null ? v.getReservedQuantity() : 0;
        int available = Math.max(0, stock - reserved);

        if (available <= 0) {
            throw new BadRequestException("Sản phẩm \"" + productName + "\"" + varLabel + " hiện đã hết hàng, không thể mua lại.");
        }
        if (available < requestedQuantity) {
            throw new BadRequestException("Sản phẩm \"" + productName + "\"" + varLabel + " chỉ còn " + available + " sản phẩm trong kho (đơn cũ đặt " + requestedQuantity + "), không đủ số lượng để mua lại.");
        }

        return toView(v);
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
        int stock = v.getStockQuantity() != null ? v.getStockQuantity() : 0;
        int available = Math.max(0, stock - reserved);
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
