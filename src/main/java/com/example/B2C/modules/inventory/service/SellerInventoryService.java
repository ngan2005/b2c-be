package com.example.B2C.modules.inventory.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.dto.SellerInventoryItemDto;
import com.example.B2C.modules.inventory.dto.SellerInventoryOverviewDto;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerInventoryService {

    private final ProductVariantRepository variantRepository;
    private final SellerRepository sellerRepository;

    @Transactional(readOnly = true)
    public SellerInventoryOverviewDto getInventoryOverview(Long userId, String filter, String keyword, int page, int size) {
        Seller seller = getActiveSeller(userId);
        Long sellerId = seller.getId();

        String normalisedFilter = (filter == null || filter.isBlank()) ? "ALL" : filter.trim().toUpperCase();
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        long totalSkus = variantRepository.countTotalSkusBySellerId(sellerId);
        long lowStockCount = variantRepository.countLowStockBySellerId(sellerId);
        long outOfStockCount = variantRepository.countOutOfStockBySellerId(sellerId);

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<ProductVariant> variantsPage = variantRepository.findSellerVariants(
                sellerId,
                normalisedFilter,
                trimmedKeyword,
                pageable
        );

        Page<SellerInventoryItemDto> dtoPage = variantsPage.map(this::toDto);

        return SellerInventoryOverviewDto.builder()
                .totalSkus(totalSkus)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .items(PageResponse.of(dtoPage))
                .build();
    }

    @Transactional
    public SellerInventoryItemDto updateVariantStock(Long userId, Long variantId, int newStock) {
        Seller seller = getActiveSeller(userId);

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id: " + variantId));

        if (variant.getProduct() == null || variant.getProduct().getSeller() == null
                || !seller.getId().equals(variant.getProduct().getSeller().getId())) {
            throw new ForbiddenException("Bạn không có quyền chỉnh sửa tồn kho của sản phẩm này");
        }

        int reserved = variant.getReservedQuantity() == null ? 0 : variant.getReservedQuantity();
        if (newStock < reserved) {
            throw new BadRequestException("Số lượng tồn kho (" + newStock + ") không thể nhỏ hơn số lượng đang giữ chỗ cho khách (" + reserved + ")");
        }

        variant.setStockQuantity(newStock);
        variant.setUpdatedAt(LocalDateTime.now());
        ProductVariant saved = variantRepository.save(variant);

        log.info("[SELLER_INVENTORY] Seller {} updated stock for variant {} ({}) to {}",
                seller.getId(), variant.getId(), variant.getSku(), newStock);

        return toDto(saved);
    }

    private Seller getActiveSeller(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Hồ sơ người bán chưa được kích hoạt hoặc đang bị khóa");
        }
        return seller;
    }

    private SellerInventoryItemDto toDto(ProductVariant v) {
        int stock = v.getStockQuantity() == null ? 0 : v.getStockQuantity();
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int available = Math.max(0, stock - reserved);

        String image = v.getImageUrl();
        if ((image == null || image.isBlank()) && v.getProduct() != null) {
            image = v.getProduct().getThumbnailUrl();
        }

        return SellerInventoryItemDto.builder()
                .variantId(v.getId())
                .productId(v.getProduct() != null ? v.getProduct().getId() : null)
                .productName(v.getProduct() != null ? v.getProduct().getName() : "—")
                .productSlug(v.getProduct() != null ? v.getProduct().getSlug() : null)
                .variantName(v.getVariantName())
                .sku(v.getSku())
                .imageUrl(image)
                .price(v.getPrice())
                .salePrice(v.getSalePrice())
                .stockQuantity(stock)
                .reservedQuantity(reserved)
                .availableStock(available)
                .soldCount(v.getSoldCount() == null ? 0 : v.getSoldCount())
                .productStatus(v.getProduct() != null && v.getProduct().getStatus() != null ? v.getProduct().getStatus().name() : "ACTIVE")
                .isActive(v.getIsActive())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
