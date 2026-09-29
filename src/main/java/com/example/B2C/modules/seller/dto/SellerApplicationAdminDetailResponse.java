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
@Schema(description = "Seller application detail response (admin-facing)")
public class SellerApplicationAdminDetailResponse {

    @Schema(description = "Application ID")
    private Long id;

    @Schema(description = "Applicant user ID")
    private Long userId;

    @Schema(description = "Applicant email")
    private String userEmail;

    @Schema(description = "Applicant full name")
    private String userFullName;

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

    @Schema(description = "Reviewer user ID")
    private Long reviewedByUserId;

    @Schema(description = "Reviewer email")
    private String reviewedByEmail;

    @Schema(description = "Review timestamp")
    private LocalDateTime reviewedAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static SellerApplicationAdminDetailResponse from(SellerApplication app) {
        SellerApplicationAdminDetailResponseBuilder builder = SellerApplicationAdminDetailResponse.builder()
                .id(app.getId())
                .shopName(app.getShopName())
                .shopDescription(app.getShopDescription())
                .phone(app.getPhone())
                .address(app.getAddress())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                .reviewedAt(app.getReviewedAt())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt());

        if (app.getUser() != null) {
            builder.userId(app.getUser().getId())
                   .userEmail(app.getUser().getEmail())
                   .userFullName(app.getUser().getFullName());
        }

        if (app.getReviewedBy() != null) {
            builder.reviewedByUserId(app.getReviewedBy().getId())
                   .reviewedByEmail(app.getReviewedBy().getEmail());
        }

        return builder.build();
    }
}
