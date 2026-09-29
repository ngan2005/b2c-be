package com.example.B2C.modules.user.dto;

import com.example.B2C.modules.user.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressRequest {

    @NotBlank(message = "Recipient name is required")
    @Size(max = 150, message = "Recipient name must not exceed 150 characters")
    private String recipientName;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 20, message = "Province code must not exceed 20 characters")
    private String provinceCode;

    @Size(max = 20, message = "District code must not exceed 20 characters")
    private String districtCode;

    @Size(max = 20, message = "Ward code must not exceed 20 characters")
    private String wardCode;

    @NotBlank(message = "Street detail is required")
    private String streetDetail;

    @NotBlank(message = "Full address is required")
    private String fullAddress;

    private AddressType type = AddressType.HOME;

    private Boolean isDefault = false;

    private BigDecimal latitude;
    private BigDecimal longitude;
}
