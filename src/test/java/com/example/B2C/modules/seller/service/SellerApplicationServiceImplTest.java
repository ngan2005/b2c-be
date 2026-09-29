package com.example.B2C.modules.seller.service;

import com.example.B2C.common.exception.BusinessException;
import com.example.B2C.common.exception.ErrorCode;
import com.example.B2C.modules.seller.dto.CreateSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.RejectSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.dto.SellerApplicationResponse;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerApplication;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerApplicationRepository;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserRole;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceImplTest {

    @Mock private SellerApplicationRepository applicationRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;

    @InjectMocks private SellerApplicationServiceImpl service;

    private User buyerUser;
    private User sellerUser;
    private User adminUser;
    private Role buyerRole;
    private Role sellerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        buyerRole = Role.builder().id(1L).code("BUYER").name("Buyer").build();
        sellerRole = Role.builder().id(2L).code("SELLER").name("Seller").build();
        adminRole = Role.builder().id(3L).code("ADMIN").name("Admin").build();

        buyerUser = User.builder()
                .id(10L).email("buyer@test.com").fullName("Buyer Test")
                .role(buyerRole).roles(new HashSet<>())
                .passwordHash("hash")
                .build();
        buyerUser.getRoles().add(buyerRole);

        sellerUser = User.builder()
                .id(11L).email("seller@test.com").fullName("Seller Test")
                .role(sellerRole).roles(new HashSet<>())
                .passwordHash("hash")
                .build();
        sellerUser.getRoles().add(sellerRole);

        adminUser = User.builder()
                .id(99L).email("admin@test.com").fullName("Admin Test")
                .role(adminRole).roles(new HashSet<>())
                .passwordHash("hash")
                .build();
        adminUser.getRoles().add(adminRole);
    }

    // ---------------------------------------------------------------------------------------------
    // Submit tests
    // ---------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("submit()")
    class Submit {

        private CreateSellerApplicationRequest validRequest() {
            return CreateSellerApplicationRequest.builder()
                    .shopName("My Shop")
                    .shopDescription("Best shop ever")
                    .phone("0901234567")
                    .address("123 Main St")
                    .businessType(BusinessType.INDIVIDUAL)
                    .build();
        }

        @Test
        @DisplayName("BUYER submits successfully -> SellerApplication saved with PENDING AND Seller row created with PENDING")
        void buyerSubmits_createsApplicationAndSellerRows() {
            CreateSellerApplicationRequest req = validRequest();

            when(userRepository.findById(10L)).thenReturn(Optional.of(buyerUser));
            when(sellerRepository.existsByUserId(10L)).thenReturn(false);
            when(applicationRepository.existsByUserIdAndStatusIn(eq(10L), any())).thenReturn(false);
            when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> {
                Seller s = inv.getArgument(0);
                s.setId(100L);
                return s;
            });
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(200L);
                return a;
            });

            SellerApplicationResponse response = service.submit(10L, req);

            assertThat(response.getStatus()).isEqualTo(SellerApplicationStatus.PENDING);
            assertThat(response.getShopName()).isEqualTo("My Shop");

            ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
            verify(sellerRepository).save(sellerCaptor.capture());
            Seller savedSeller = sellerCaptor.getValue();
            assertThat(savedSeller.getStatus()).isEqualTo(SellerStatus.PENDING);
            assertThat(savedSeller.getShopName()).isEqualTo("My Shop");
            assertThat(savedSeller.getSlug()).isNotBlank();

            ArgumentCaptor<SellerApplication> appCaptor = ArgumentCaptor.forClass(SellerApplication.class);
            verify(applicationRepository).save(appCaptor.capture());
            assertThat(appCaptor.getValue().getStatus()).isEqualTo(SellerApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Non-BUYER cannot submit -> BusinessException BUYER_ROLE_REQUIRED")
        void nonBuyerCannotSubmit() {
            User noRoleUser = User.builder()
                    .id(20L).email("norole@test.com").fullName("No Role")
                    .role(null).roles(new HashSet<>()).passwordHash("hash")
                    .build();

            when(userRepository.findById(20L)).thenReturn(Optional.of(noRoleUser));

            assertThatThrownBy(() -> service.submit(20L, validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.BUYER_ROLE_REQUIRED));
        }

        @Test
        @DisplayName("User who already has SELLER role cannot submit -> ErrorCode USER_ALREADY_SELLER")
        void userAlreadySellerCannotSubmit() {
            // A promoted seller holds BOTH roles: BUYER from registration, SELLER added on
            // approval. The BUYER guard must not short-circuit ahead of the seller guard,
            // otherwise a real seller is told they are "missing a BUYER role".
            Role freshSellerRole = Role.builder().id(2L).code("SELLER").name("Seller").build();
            Role freshBuyerRole = Role.builder().id(1L).code("BUYER").name("Buyer").build();
            User sellerUserForTest = User.builder()
                    .id(11L).email("seller@test.com").fullName("Seller Test")
                    .role(freshSellerRole)
                    .roles(new HashSet<>())  // isolated set, no shared state across tests
                    .passwordHash("hash")
                    .build();
            sellerUserForTest.getRoles().add(freshSellerRole);
            sellerUserForTest.getRoles().add(freshBuyerRole);

            when(userRepository.findById(11L)).thenReturn(Optional.of(sellerUserForTest));

            assertThatThrownBy(() -> service.submit(11L, validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_ALREADY_SELLER));

            // Guard must trip on the role alone, before any seller-row lookup.
            verify(sellerRepository, never()).existsByUserId(anyLong());
        }

        @Test
        @DisplayName("User with existing PENDING application cannot submit duplicate -> ErrorCode SELLER_APPLICATION_ALREADY_PENDING")
        void existingPendingApplicationBlocksDuplicate() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(buyerUser));
            when(sellerRepository.existsByUserId(10L)).thenReturn(false);
            when(applicationRepository.existsByUserIdAndStatusIn(eq(10L), any())).thenReturn(true);

            assertThatThrownBy(() -> service.submit(10L, validRequest()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SELLER_APPLICATION_ALREADY_PENDING));
        }

        @Test
        @DisplayName("REJECTED applicant can submit again — PENDING seller row was deleted on reject")
        void rejectedApplicantCanResubmit() {
            // Regression guard for orphan-seller fix: after rejection the PENDING seller row
            // is deleted so existsByUserId returns false, allowing re-submission.
            CreateSellerApplicationRequest req = validRequest();

            when(userRepository.findById(10L)).thenReturn(Optional.of(buyerUser));
            when(sellerRepository.existsByUserId(10L)).thenReturn(false); // seller deleted on reject
            when(applicationRepository.existsByUserIdAndStatusIn(eq(10L), any())).thenReturn(false);
            when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(400L);
                return a;
            });

            SellerApplicationResponse response = service.submit(10L, req);

            assertThat(response.getStatus()).isEqualTo(SellerApplicationStatus.PENDING);
            // Key assertion: sellerRepository.save was called — a NEW seller row was created
            verify(sellerRepository).save(any(Seller.class));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Approve tests
    // ---------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("approve()")
    class Approve {

        private SellerApplication buildPendingApplication(Long id, User user) {
            return SellerApplication.builder()
                    .id(id).user(user)
                    .shopName("Shop").phone("0900").address("addr")
                    .status(SellerApplicationStatus.PENDING)
                    .build();
        }

        private Seller buildPendingSeller(Long userId) {
            User user = userId.equals(10L) ? buyerUser : sellerUser;
            return Seller.builder()
                    .id(50L).user(user)
                    .shopName("Shop").slug("shop-abc12345")
                    .businessType(BusinessType.INDIVIDUAL)
                    .status(SellerStatus.PENDING)
                    .build();
        }

        @Test
        @DisplayName("Approve adds SELLER role when user does not have it -> UserRole saved, seller ACTIVE, application APPROVED")
        void approveAddsSellerRoleAndActivatesProfile() {
            SellerApplication app = buildPendingApplication(1L, buyerUser);
            Seller seller = buildPendingSeller(10L);

            when(applicationRepository.findByIdWithLock(1L)).thenReturn(Optional.of(app));
            when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
            when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(seller));
            when(roleRepository.findByCode("SELLER")).thenReturn(Optional.of(sellerRole));
            when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(1L);
                return a;
            });

            SellerApplicationAdminDetailResponse result = service.approve(99L, 1L);

            assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.APPROVED);
            assertThat(result.getReviewedByUserId()).isEqualTo(99L);
            assertThat(result.getReviewedAt()).isNotNull();

            ArgumentCaptor<UserRole> urCaptor = ArgumentCaptor.forClass(UserRole.class);
            verify(userRoleRepository).save(urCaptor.capture());
            UserRole savedUserRole = urCaptor.getValue();
            assertThat(savedUserRole.getUserId()).isEqualTo(10L);
            assertThat(savedUserRole.getRoleId()).isEqualTo(2L);

            ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
            verify(sellerRepository).save(sellerCaptor.capture());
            assertThat(sellerCaptor.getValue().getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(sellerCaptor.getValue().getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("Approve does NOT create duplicate SELLER role when user already has it -> userRoleRepository.save never called")
        void approveDoesNotDuplicateSellerRole() {
            SellerApplication app = buildPendingApplication(2L, sellerUser);
            Seller seller = buildPendingSeller(11L);

            when(applicationRepository.findByIdWithLock(2L)).thenReturn(Optional.of(app));
            when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
            when(sellerRepository.findByUserId(11L)).thenReturn(Optional.of(seller));
            when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(2L);
                return a;
            });

            service.approve(99L, 2L);

            verify(userRoleRepository, never()).save(any(UserRole.class));
        }

        @Test
        @DisplayName("Approve already-APPROVED application throws ErrorCode SELLER_APPLICATION_ALREADY_APPROVED")
        void approveAlreadyApprovedThrows() {
            SellerApplication app = buildPendingApplication(3L, buyerUser);
            app.setStatus(SellerApplicationStatus.APPROVED);
            app.setReviewedAt(LocalDateTime.now());
            app.setReviewedBy(adminUser);

            when(applicationRepository.findByIdWithLock(3L)).thenReturn(Optional.of(app));

            assertThatThrownBy(() -> service.approve(99L, 3L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.SELLER_APPLICATION_ALREADY_APPROVED));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Reject tests
    // ---------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("reject()")
    class Reject {

        private SellerApplication buildPendingApplication(Long id, User user) {
            return SellerApplication.builder()
                    .id(id).user(user)
                    .shopName("Shop").phone("0900").address("addr")
                    .status(SellerApplicationStatus.PENDING)
                    .build();
        }

        @Test
        @DisplayName("Reject requires non-blank reason -> ErrorCode REJECTION_REASON_REQUIRED")
        void rejectRequiresReason() {
            // REJECTION_REASON_REQUIRED is thrown BEFORE any repository call, so no stubs needed.
            // lenient() suppresses the strict-stubbing warning from MockitoExtension.
            lenient().when(applicationRepository.findByIdWithLock(anyLong()))
                    .thenReturn(Optional.of(buildPendingApplication(1L, buyerUser)));

            RejectSellerApplicationRequest blankReq =
                    RejectSellerApplicationRequest.builder().rejectionReason("").build();
            assertThatThrownBy(() -> service.reject(99L, 1L, blankReq))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REJECTION_REASON_REQUIRED));

            RejectSellerApplicationRequest nullReq =
                    RejectSellerApplicationRequest.builder().rejectionReason(null).build();
            assertThatThrownBy(() -> service.reject(99L, 1L, nullReq))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.REJECTION_REASON_REQUIRED));
        }

        @Test
        @DisplayName("Reject sets REJECTED + reason + reviewer + reviewedAt, does NOT touch user roles, deletes PENDING seller row")
        void rejectDeletesPendingSellerRow() {
            SellerApplication app = buildPendingApplication(1L, buyerUser);
            Seller pendingSeller = Seller.builder()
                    .id(50L).user(buyerUser)
                    .shopName("Shop").slug("shop-abc12345")
                    .businessType(BusinessType.INDIVIDUAL)
                    .status(SellerStatus.PENDING)
                    .build();

            when(applicationRepository.findByIdWithLock(1L)).thenReturn(Optional.of(app));
            when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
            when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(pendingSeller));
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(1L);
                return a;
            });

            RejectSellerApplicationRequest req =
                    RejectSellerApplicationRequest.builder().rejectionReason("Shop name taken").build();
            SellerApplicationAdminDetailResponse result = service.reject(99L, 1L, req);

            assertThat(result.getStatus()).isEqualTo(SellerApplicationStatus.REJECTED);
            assertThat(result.getRejectionReason()).isEqualTo("Shop name taken");
            assertThat(result.getReviewedByUserId()).isEqualTo(99L);
            assertThat(result.getReviewedAt()).isNotNull();

            // No role was added
            verify(userRoleRepository, never()).save(any(UserRole.class));

            // The PENDING seller row was deleted
            verify(sellerRepository).delete(any(Seller.class));
        }

        @Test
        @DisplayName("Reject does NOT delete an ACTIVE seller row (profile is protected)")
        void rejectDoesNotDeleteActiveSellerRow() {
            SellerApplication app = buildPendingApplication(1L, buyerUser);
            Seller activeSeller = Seller.builder()
                    .id(50L).user(buyerUser)
                    .shopName("Shop").slug("shop-abc12345")
                    .businessType(BusinessType.INDIVIDUAL)
                    .status(SellerStatus.ACTIVE) // ACTIVE — must NOT be deleted
                    .build();

            when(applicationRepository.findByIdWithLock(1L)).thenReturn(Optional.of(app));
            when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
            when(sellerRepository.findByUserId(10L)).thenReturn(Optional.of(activeSeller));
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(1L);
                return a;
            });

            RejectSellerApplicationRequest req =
                    RejectSellerApplicationRequest.builder().rejectionReason("Invalid details").build();
            service.reject(99L, 1L, req);

            // ACTIVE seller row is protected — delete must never be called
            verify(sellerRepository, never()).delete(any(Seller.class));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Slug generation tests
    // ---------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("Slug generation")
    class SlugGeneration {

        @Test
        @DisplayName("Non-Latin shop name (Vietnamese) must not produce blank/NULL slug")
        void nonLatinShopNameProducesValidSlug() {
            CreateSellerApplicationRequest req = CreateSellerApplicationRequest.builder()
                    .shopName("Cửa Hàng Của Tôi")
                    .phone("0901234567")
                    .address("123 Main St")
                    .businessType(BusinessType.INDIVIDUAL)
                    .build();

            when(userRepository.findById(10L)).thenReturn(Optional.of(buyerUser));
            when(sellerRepository.existsByUserId(10L)).thenReturn(false);
            when(applicationRepository.existsByUserIdAndStatusIn(eq(10L), any())).thenReturn(false);
            when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> {
                Seller s = inv.getArgument(0);
                s.setId(100L);
                return s;
            });
            when(applicationRepository.save(any(SellerApplication.class))).thenAnswer(inv -> {
                SellerApplication a = inv.getArgument(0);
                a.setId(200L);
                return a;
            });

            service.submit(10L, req);

            ArgumentCaptor<Seller> captor = ArgumentCaptor.forClass(Seller.class);
            verify(sellerRepository).save(captor.capture());
            String slug = captor.getValue().getSlug();

            assertThat(slug).isNotBlank();
            assertThat(slug).isNotNull();
            // uniqueSlug appends 8-char UUID suffix, so slug must be longer than a base word
            assertThat(slug.length()).isGreaterThan(4);
        }
    }
}
