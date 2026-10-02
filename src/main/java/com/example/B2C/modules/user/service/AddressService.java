package com.example.B2C.modules.user.service;

import com.example.B2C.modules.user.dto.AddressDto;
import com.example.B2C.modules.user.dto.AddressRequest;

import java.util.List;

public interface AddressService {

    List<AddressDto> getMyAddresses(Long userId);

    AddressDto getAddressById(Long userId, Long addressId);

    AddressDto createAddress(Long userId, AddressRequest request);

    AddressDto updateAddress(Long userId, Long addressId, AddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    AddressDto setDefaultAddress(Long userId, Long addressId);
}
