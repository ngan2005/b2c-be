package com.example.B2C.modules.seller.dto;

import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.SellerStatus;
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
public class AdminSellerDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String shopName;
    private String slug;
    private String logoUrl;
    private String description;
    private BusinessType businessType;
    private String taxCode;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer totalProduct;
    private SellerStatus status;
    private String suspensionReason;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
