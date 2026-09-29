package com.example.B2C.modules.user.dto;

import com.example.B2C.modules.user.entity.Address;
import com.example.B2C.modules.user.entity.AddressType;
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
public class AddressDto {

    private Long id;
    private Long userId;
    private String recipientName;
    private String phone;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
    private String streetDetail;
    private String fullAddress;
    private AddressType type;
    private Boolean isDefault;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressDto from(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .userId(address.getUser() != null ? address.getUser().getId() : null)
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .provinceCode(address.getProvinceCode())
                .districtCode(address.getDistrictCode())
                .wardCode(address.getWardCode())
                .streetDetail(address.getStreetDetail())
                .fullAddress(address.getFullAddress())
                .type(address.getType())
                .isDefault(address.getIsDefault())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
