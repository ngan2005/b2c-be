package com.example.B2C.modules.wishlist.dto;

import com.example.B2C.modules.wishlist.entity.Wishlist;
import com.example.B2C.modules.catalog.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDto {

    private Long id;
    private Long productId;
    private String productSlug;
    private String productName;
    private String productThumbnail;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sellerName;
    private Long sellerId;
    private String status;
    private LocalDateTime addedAt;

    public static WishlistItemDto from(Wishlist w) {
        Product p = w.getProduct();
        return WishlistItemDto.builder()
            .id(w.getId())
            .productId(p.getId())
            .productSlug(p.getSlug())
            .productName(p.getName())
            .productThumbnail(p.getThumbnailUrl())
            .minPrice(p.getMinPrice())
            .maxPrice(p.getMaxPrice())
            .sellerName(p.getSeller() != null ? p.getSeller().getShopName() : null)
            .sellerId(p.getSeller() != null ? p.getSeller().getId() : null)
            .status(p.getStatus() != null ? p.getStatus().name() : null)
            .addedAt(w.getCreatedAt())
            .build();
    }
}
