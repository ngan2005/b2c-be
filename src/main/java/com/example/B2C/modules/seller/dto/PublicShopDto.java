package com.example.B2C.modules.seller.dto;

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
public class PublicShopDto {
    private Long id;
    private String name;
    private String shopName;
    private String slug;
    private String logoUrl;
    private String bannerUrl;
    private String description;
    private String businessType;
    private boolean verified;
    private BigDecimal rating;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Long productCount;
    private String responseRate;
    private String responseTime;
    private Integer followerCount;
    private String address;
    private LocalDateTime createdAt;
    private Boolean isOnline;
    private Boolean isFollowing;
}
