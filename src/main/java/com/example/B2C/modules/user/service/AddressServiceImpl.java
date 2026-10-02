package com.example.B2C.modules.user.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.user.dto.AddressDto;
import com.example.B2C.modules.user.dto.AddressRequest;
import com.example.B2C.modules.user.entity.Address;
import com.example.B2C.modules.user.entity.AddressType;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.AddressRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getMyAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescUpdatedAtDesc(userId)
                .stream()
                .map(AddressDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        return AddressDto.from(address);
    }

    @Override
    @Transactional
    public AddressDto createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault())
                || addressRepository.countByUserId(userId) == 0;

        if (shouldBeDefault) {
            addressRepository.clearAllDefaults(userId);
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .provinceCode(request.getProvinceCode())
                .districtCode(request.getDistrictCode())
                .wardCode(request.getWardCode())
                .streetDetail(request.getStreetDetail())
                .fullAddress(request.getFullAddress())
                .type(request.getType() != null ? request.getType() : AddressType.HOME)
                .isDefault(shouldBeDefault)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Address saved = addressRepository.save(address);
        log.info("Address created for user {}: addressId={}, default={}", userId, saved.getId(), shouldBeDefault);
        return AddressDto.from(saved);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (address.getUser() == null || !address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Address does not belong to user");
        }

        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setProvinceCode(request.getProvinceCode());
        address.setDistrictCode(request.getDistrictCode());
        address.setWardCode(request.getWardCode());
        address.setStreetDetail(request.getStreetDetail());
        address.setFullAddress(request.getFullAddress());
        if (request.getType() != null) {
            address.setType(request.getType());
        }
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultExcept(userId, addressId);
            address.setIsDefault(true);
        }

        Address saved = addressRepository.save(address);
        log.info("Address updated for user {}: addressId={}", userId, addressId);
        return AddressDto.from(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);
        addressRepository.flush();

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserIdOrderByIsDefaultDescUpdatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                Address first = remaining.get(0);
                first.setIsDefault(true);
                addressRepository.save(first);
                log.info("Promoted new default address {} for user {}", first.getId(), userId);
            }
        }
        log.info("Address {} deleted for user {}", addressId, userId);
    }

    @Override
    @Transactional
    public AddressDto setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            return AddressDto.from(address);
        }

        addressRepository.clearAllDefaults(userId);
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);
        log.info("Address {} set as default for user {}", addressId, userId);
        return AddressDto.from(saved);
    }
}
