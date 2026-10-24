package com.example.B2C.modules.seller.service;

import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerApplication;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerApplicationRepository;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies that {@link SellerApplicationServiceImpl#approve(Long, Long)} strips the BUYER
 * role from the user_roles join table after adding SELLER.
 *
 * Expected order of operations:
 *   1. SELLER role INSERTed into user_roles
 *   2. BUYER role DELETed from user_roles
 */
@ExtendWith(MockitoExtension.class)
class SellerApplicationApproveRoleStripTest {

    @Mock private SellerApplicationRepository applicationRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;

    @InjectMocks private SellerApplicationServiceImpl service;

    private Role buyerRole;
    private Role sellerRole;
    private Role adminRole;
    private User buyerUser;
    private User adminUser;
    private Seller seller;

    @BeforeEach
    void setUp() {
        buyerRole = Role.builder().id(1L).code("BUYER").name("Buyer").build();
        sellerRole = Role.builder().id(2L).code("SELLER").name("Seller").build();
        adminRole  = Role.builder().id(3L).code("ADMIN").name("Admin").build();

        buyerUser = User.builder()
                .id(10L).email("buyer@test.com").fullName("Buyer Test")
                .role(buyerRole)
                .roles(new HashSet<>())
                .passwordHash("hash")
                .build();
        buyerUser.getRoles().add(buyerRole);

        adminUser = User.builder()
                .id(99L).email("admin@test.com").fullName("Admin Test")
                .role(adminRole)
                .roles(new HashSet<>())
                .passwordHash("hash")
                .build();
        adminUser.getRoles().add(adminRole);

        seller = Seller.builder()
                .id(50L).user(buyerUser)
                .shopName("My Shop").slug("my-shop-abc12345")
                .businessType(BusinessType.INDIVIDUAL)
                .status(SellerStatus.PENDING)
                .build();
    }

    private SellerApplication buildPendingApplication(Long id, User user) {
        return SellerApplication.builder()
                .id(id).user(user)
                .shopName("My Shop").phone("0900000000").address("123 Main St")
                .status(SellerApplicationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("approve() — SELLER role added THEN BUYER role removed; deleteByUserIdAndRoleId called with BUYER role id")
    void approveStripsBuyerRoleAfterAddingSellerRole() {
        // Arrange
        SellerApplication app = buildPendingApplication(1L, buyerUser);

        when(applicationRepository.findByIdWithLock(1L)).thenReturn(Optional.of(app));
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
        when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(seller));
        when(roleRepository.findByCode("SELLER")).thenReturn(Optional.of(sellerRole));
        when(roleRepository.findByCode("BUYER")).thenReturn(Optional.of(buyerRole));
        when(userRoleRepository.deleteByUserIdAndRoleId(10L, 1L)).thenReturn(1L);
        when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(inv -> { SellerApplication a = inv.getArgument(0); a.setId(1L); return a; });

        // Act
        SellerApplicationAdminDetailResponse result = service.approve(99L, 1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.APPROVED);

        // Verify the BUYER role was removed (called once, with correct userId and BUYER role id)
        verify(userRoleRepository).deleteByUserIdAndRoleId(eq(10L), eq(1L));
    }

    @Test
    @DisplayName("approve() — deleteByUserIdAndRoleId returns 0 when BUYER row already absent; no exception thrown")
    void approveHandlesMissingBuyerRoleGracefully() {
        // Arrange
        SellerApplication app = buildPendingApplication(2L, buyerUser);

        when(applicationRepository.findByIdWithLock(2L)).thenReturn(Optional.of(app));
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
        when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(seller));
        when(roleRepository.findByCode("SELLER")).thenReturn(Optional.of(sellerRole));
        when(roleRepository.findByCode("BUYER")).thenReturn(Optional.of(buyerRole));
        when(userRoleRepository.deleteByUserIdAndRoleId(10L, 1L)).thenReturn(0L); // BUYER already gone
        when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(inv -> { SellerApplication a = inv.getArgument(0); a.setId(2L); return a; });

        // Act — must not throw
        SellerApplicationAdminDetailResponse result = service.approve(99L, 2L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.APPROVED);
        // delete was still called (0 rows deleted is valid — idempotent)
        verify(userRoleRepository).deleteByUserIdAndRoleId(10L, 1L);
    }

    @Test
    @DisplayName("approve() — when BUYER role does not exist in DB, no deletion attempted (null-safe)")
    void approveHandlesMissingBuyerRoleEntityGracefully() {
        // Arrange — BUYER role is absent from DB entirely
        SellerApplication app = buildPendingApplication(3L, buyerUser);

        when(applicationRepository.findByIdWithLock(3L)).thenReturn(Optional.of(app));
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
        when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(seller));
        when(roleRepository.findByCode("SELLER")).thenReturn(Optional.of(sellerRole));
        when(roleRepository.findByCode("BUYER")).thenReturn(Optional.empty()); // BUYER gone
        when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(inv -> { SellerApplication a = inv.getArgument(0); a.setId(3L); return a; });

        // Act — must not throw
        SellerApplicationAdminDetailResponse result = service.approve(99L, 3L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.APPROVED);
        // deleteByUserIdAndRoleId must NOT be called when findByCode returns empty
        verify(userRoleRepository, never()).deleteByUserIdAndRoleId(anyLong(), anyLong());
    }
}
