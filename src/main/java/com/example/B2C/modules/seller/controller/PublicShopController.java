package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.catalog.service.ProductService;
import com.example.B2C.modules.review.dto.ReviewDto;
import com.example.B2C.modules.review.service.ReviewService;
import com.example.B2C.modules.seller.dto.PublicShopDto;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.realtime.UserPresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/shops")
@RequiredArgsConstructor
@Tag(name = "Public Shop", description = "Public store/shop endpoints for buyers")
public class PublicShopController {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserPresenceService userPresenceService;

    @GetMapping("/{slug}")
    @Operation(summary = "Get public shop profile by slug")
    public ResponseEntity<ApiResponse<PublicShopDto>> getShopBySlug(@PathVariable String slug) {
        Seller seller = sellerRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with slug: " + slug));

        long productCount = productRepository.countBySellerIdAndStatusAndDeletedAtIsNull(seller.getId(), ProductStatus.ACTIVE);
        boolean isOnline = seller.getUser() != null && userPresenceService.isUserOnline(seller.getUser().getId());

        PublicShopDto dto = PublicShopDto.builder()
                .id(seller.getId())
                .name(seller.getShopName())
                .shopName(seller.getShopName())
                .slug(seller.getSlug())
                .logoUrl(seller.getLogoUrl())
                .bannerUrl(seller.getBannerUrl())
                .description(seller.getDescription())
                .businessType(seller.getBusinessType() != null ? seller.getBusinessType().name() : "INDIVIDUAL")
                .verified(seller.getStatus() == SellerStatus.ACTIVE)
                .rating(seller.getRatingAvg())
                .ratingAvg(seller.getRatingAvg())
                .ratingCount(seller.getRatingCount())
                .productCount(productCount)
                .responseRate("99%")
                .responseTime("vài phút")
                .followerCount(seller.getFollowerCount() != null ? seller.getFollowerCount() : 0)
                .address(seller.getPickupAddress() != null ? seller.getPickupAddress().getFullAddress() : null)
                .createdAt(seller.getCreatedAt())
                .isOnline(isOnline)
                .isFollowing(false)
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{slug}/products")
    @Operation(summary = "Get products of shop by slug with filters and pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryDto>>> getShopProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long categoryId
    ) {
        Seller seller = sellerRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with slug: " + slug));

        String sortBy = "created";
        String sortDir = "desc";
        if (sort != null) {
            String[] parts = sort.split("-");
            if (parts.length == 2) {
                sortBy = parts[0];
                sortDir = parts[1];
            }
        }

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .sellerId(seller.getId())
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<ProductSummaryDto> result = productService.searchProducts(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{slug}/reviews")
    @Operation(summary = "Get reviews of shop by slug")
    public ResponseEntity<ApiResponse<PageResponse<ReviewDto>>> getShopReviews(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Seller seller = sellerRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with slug: " + slug));

        PageResponse<ReviewDto> result = reviewService.listBySellerId(seller.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
