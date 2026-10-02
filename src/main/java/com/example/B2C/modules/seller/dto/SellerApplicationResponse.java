package com.example.B2C.modules.seller.dto;

import com.example.B2C.modules.seller.entity.SellerApplication;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.entity.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Seller application response (buyer-facing)")
public class SellerApplicationResponse {

    @Schema(description = "Application ID")
    private Long id;

    @Schema(description = "Shop name")
    private String shopName;

    @Schema(description = "Shop description")
    private String shopDescription;

    @Schema(description = "Contact phone")
    private String phone;

    @Schema(description = "Business address")
    private String address;

    @Schema(description = "Business type")
    private BusinessType businessType;

    @Schema(description = "Application status")
    private SellerApplicationStatus status;

    @Schema(description = "Rejection reason (if rejected)")
    private String rejectionReason;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static SellerApplicationResponse from(SellerApplication app) {
        return SellerApplicationResponse.builder()
                .id(app.getId())
                .shopName(app.getShopName())
                .shopDescription(app.getShopDescription())
                .phone(app.getPhone())
                .address(app.getAddress())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
