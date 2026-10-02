package com.example.B2C.modules.seller.service;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getMyProfile(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        return SellerProfileResponse.from(seller);
    }
}
