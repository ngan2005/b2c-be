package com.example.B2C.modules.seller.dto;

import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.SellerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Seller profile response")
public class SellerProfileResponse {

    @Schema(description = "Seller profile ID")
    private Long id;

    @Schema(description = "Owner user ID")
    private Long userId;

    @Schema(description = "Owner email")
    private String userEmail;

    @Schema(description = "Owner full name")
    private String userFullName;

    @Schema(description = "Shop name")
    private String shopName;

    @Schema(description = "Shop slug (URL-friendly)")
    private String slug;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Banner URL")
    private String bannerUrl;

    @Schema(description = "Shop description")
    private String description;

    @Schema(description = "Business type")
    private BusinessType businessType;

    @Schema(description = "Average rating")
    private BigDecimal ratingAvg;

    @Schema(description = "Number of ratings")
    private Integer ratingCount;

    @Schema(description = "Follower count")
    private Integer followerCount;

    @Schema(description = "Total products")
    private Integer totalProduct;

    @Schema(description = "Seller status")
    private SellerStatus status;

    @Schema(description = "Approval timestamp")
    private LocalDateTime approvedAt;

    @Schema(description = "Profile creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static SellerProfileResponse from(Seller seller) {
        SellerProfileResponseBuilder builder = SellerProfileResponse.builder()
                .id(seller.getId())
                .shopName(seller.getShopName())
                .slug(seller.getSlug())
                .logoUrl(seller.getLogoUrl())
                .bannerUrl(seller.getBannerUrl())
                .description(seller.getDescription())
                .businessType(seller.getBusinessType())
                .ratingAvg(seller.getRatingAvg())
                .ratingCount(seller.getRatingCount())
                .followerCount(seller.getFollowerCount())
                .totalProduct(seller.getTotalProduct())
                .status(seller.getStatus())
                .approvedAt(seller.getApprovedAt())
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt());

        if (seller.getUser() != null) {
            builder.userId(seller.getUser().getId())
                   .userEmail(seller.getUser().getEmail())
                   .userFullName(seller.getUser().getFullName());
        }

        return builder.build();
    }
}
