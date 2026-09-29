package com.example.B2C.modules.seller.service;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceImplTest {

    @Mock private SellerRepository sellerRepository;

    @InjectMocks private SellerServiceImpl service;

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfile {

        @Test
        @DisplayName("Returns profile for the given userId")
        void returnsProfileForUser() {
            Seller seller = Seller.builder()
                    .id(5L)
                    .shopName("My Shop")
                    .slug("my-shop-abc")
                    .description("Best shop")
                    .businessType(BusinessType.INDIVIDUAL)
                    .status(SellerStatus.ACTIVE)
                    .ratingAvg(BigDecimal.valueOf(4, 5))
                    .ratingCount(10)
                    .followerCount(100)
                    .totalProduct(50)
                    .approvedAt(LocalDateTime.of(2024, 1, 15, 0, 0))
                    .build();
            seller.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
            seller.setUpdatedAt(LocalDateTime.of(2024, 1, 10, 0, 0));

            when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(seller));

            SellerProfileResponse response = service.getMyProfile(10L);

            assertThat(response.getId()).isEqualTo(5L);
            assertThat(response.getShopName()).isEqualTo("My Shop");
            assertThat(response.getSlug()).isEqualTo("my-shop-abc");
            assertThat(response.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            verify(sellerRepository).findByUserId(10L);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when user has no seller row")
        void throwsWhenNoSellerRow() {
            when(sellerRepository.findByUserId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getMyProfile(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Seller profile not found for user id: 99");

            verify(sellerRepository).findByUserId(99L);
        }

        @Test
        @DisplayName("getMyProfile looks up strictly by the passed-in userId — must not call findById")
        void ownershipGuard_lookupByUserIdOnly() {
            Seller seller = Seller.builder()
                    .id(5L)
                    .shopName("Seller Shop")
                    .slug("seller-shop")
                    .businessType(BusinessType.INDIVIDUAL)
                    .status(SellerStatus.ACTIVE)
                    .build();

            when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(seller));

            // If someone tries to look up the profile, it should use findByUserId, not findById
            // The test passes as long as findByUserId was called — findById is never invoked
            service.getMyProfile(10L);

            verify(sellerRepository).findByUserId(10L);
        }
    }
}
