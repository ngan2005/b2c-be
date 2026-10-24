// [SELLER-BUYER-UPGRADE]
package com.example.B2C.modules.seller.dto;

import com.example.B2C.modules.seller.entity.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for a buyer to quick-upgrade to seller — bypasses admin review")
public class QuickUpgradeRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 150, message = "Shop name must not exceed 150 characters")
    @Schema(description = "Desired shop name", example = "My Awesome Shop")
    private String shopName;

    @NotNull(message = "Business type is required")
    @Schema(description = "Type of business", example = "INDIVIDUAL")
    private BusinessType businessType;

    @Schema(description = "Shop description", example = "We sell the best products online")
    private String shopDescription;

    @Schema(description = "Contact phone number", example = "0901234567")
    private String phone;

    @Schema(description = "Business address", example = "123 Main Street, District 1, Ho Chi Minh City")
    private String address;

    @Size(min = 9, max = 12, message = "ID card number must be between 9 and 12 digits")
    @Schema(description = "ID card / citizen identity number", example = "012345678912")
    private String idCardNumber;
}
