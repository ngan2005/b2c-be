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
@Schema(description = "Request to submit a seller application")
public class CreateSellerApplicationRequest {

    @NotBlank(message = "Shop name is required")
    @Size(max = 150, message = "Shop name must not exceed 150 characters")
    @Schema(description = "Desired shop name", example = "My Awesome Shop")
    private String shopName;

    @Schema(description = "Shop description", example = "We sell the best products online")
    private String shopDescription;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(description = "Contact phone number", example = "0901234567")
    private String phone;

    @NotBlank(message = "Address is required")
    @Schema(description = "Business address", example = "123 Main Street, District 1, Ho Chi Minh City")
    private String address;

    @NotNull(message = "Business type is required")
    @Schema(description = "Type of business", example = "INDIVIDUAL")
    private BusinessType businessType;
}
