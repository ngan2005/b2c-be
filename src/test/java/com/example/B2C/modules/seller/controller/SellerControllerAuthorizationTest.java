package com.example.B2C.modules.seller.controller;

import com.example.B2C.common.exception.BusinessException;
import com.example.B2C.common.exception.ErrorCode;
import com.example.B2C.common.exception.GlobalExceptionHandler;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.seller.dto.CreateSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.RejectSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.dto.SellerApplicationResponse;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.service.SellerApplicationService;
import com.example.B2C.modules.seller.service.SellerService;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Authorization tests for the seller controllers.
 *
 * <p>These exercise the real {@code @PreAuthorize} annotations: each controller is wrapped in
 * a proxy carrying Spring Security's own {@code AuthorizationManagerBeforeMethodInterceptor},
 * so a wrong or missing annotation fails these tests. A hand-rolled filter re-implementing
 * {@code hasRole(...)} would not - it would only assert that the test's own copy of the rules
 * works, and would stay green even if the controller annotation said something else.
 *
 * <p>Access-denied translation is provided by a small stand-in for
 * {@code ExceptionTranslationFilter}, which is the component that turns a denied
 * authorization into a 401/403 response in production.
 */
@ExtendWith(MockitoExtension.class)
class SellerControllerAuthorizationTest {

    @Mock private SellerApplicationService sellerApplicationService;
    @Mock private SellerService sellerService;
    @Mock private CurrentUserProvider currentUserProvider;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        secured(new SellerApplicationController(sellerApplicationService, currentUserProvider)),
                        secured(new AdminSellerApplicationController(sellerApplicationService, currentUserProvider)),
                        secured(new SellerController(sellerService, currentUserProvider)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .addFilters(new ExceptionTranslationFilter())
                .build();
    }

    /** Wraps a controller in a proxy that enforces its {@code @PreAuthorize} annotations. */
    private <T> T secured(T controller) {
        ProxyFactory factory = new ProxyFactory(controller);
        factory.setProxyTargetClass(true);
        factory.addAdvisor(AuthorizationManagerBeforeMethodInterceptor.preAuthorize());
        return (T) factory.getProxy();
    }

    /**
     * SecurityContextHolder is a ThreadLocal and JUnit reuses the thread, so a principal
     * left behind by one test would silently authenticate the next one.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Mirrors the two gates that produce 401/403 in production: the
     * {@code anyRequest().authenticated()} rule rejects anonymous callers with 401, and
     * {@code ExceptionTranslationFilter} turns a denied authorization into 403.
     */
    private static class ExceptionTranslationFilter implements Filter {
        @Override
        public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res,
                             FilterChain chain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            // anyRequest().authenticated() - runs before method security, so an anonymous
            // caller is rejected as 401 even when the endpoint would also deny by role.
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            if (current == null || !current.isAuthenticated()) {
                write(response, 401, "Unauthorized");
                return;
            }

            try {
                chain.doFilter(request, response);
            } catch (AuthenticationCredentialsNotFoundException ex) {
                write(response, 401, "Unauthorized");
            } catch (AccessDeniedException ex) {
                write(response, 403, "Access denied");
            }
        }

        private void write(HttpServletResponse response, int status, String message) throws IOException {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
        }
    }

    // -------------------------------------------------------------------------------------------
    // Fixtures - authorities come from CustomUserDetails, the production source
    // -------------------------------------------------------------------------------------------

    private CustomUserDetails buyer() {
        Role r = Role.builder().id(1L).code("BUYER").name("Buyer").build();
        User u = User.builder().id(10L).email("buyer@test.com").fullName("Buyer Test")
                .role(r).roles(new HashSet<>()).passwordHash("hash").build();
        u.getRoles().add(r);
        return new CustomUserDetails(u);
    }

    private CustomUserDetails seller() {
        Role r = Role.builder().id(2L).code("SELLER").name("Seller").build();
        User u = User.builder().id(11L).email("seller@test.com").fullName("Seller Test")
                .role(r).roles(new HashSet<>()).passwordHash("hash").build();
        u.getRoles().add(r);
        return new CustomUserDetails(u);
    }

    private CustomUserDetails admin() {
        Role r = Role.builder().id(3L).code("ADMIN").name("Admin").build();
        User u = User.builder().id(99L).email("admin@test.com").fullName("Admin Test")
                .role(r).roles(new HashSet<>()).passwordHash("hash").build();
        u.getRoles().add(r);
        return new CustomUserDetails(u);
    }

    /** A user holding BUYER and SELLER at once - the post-approval state. */
    private CustomUserDetails buyerAndSeller() {
        Role b = Role.builder().id(1L).code("BUYER").name("Buyer").build();
        Role s = Role.builder().id(2L).code("SELLER").name("Seller").build();
        User u = User.builder().id(12L).email("both@test.com").fullName("Buyer Seller")
                .role(b).roles(new HashSet<>()).passwordHash("hash").build();
        u.getRoles().add(b);
        u.getRoles().add(s);
        return new CustomUserDetails(u);
    }

    /** Installs the principal on the SecurityContext for the duration of the request. */
    private static RequestPostProcessor as(CustomUserDetails userDetails) {
        return request -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    private CreateSellerApplicationRequest validApplicationRequest() {
        return CreateSellerApplicationRequest.builder()
                .shopName("My Shop")
                .shopDescription("Best shop")
                .phone("0901234567")
                .address("123 Main St")
                .businessType(BusinessType.INDIVIDUAL)
                .build();
    }

    // -------------------------------------------------------------------------------------------
    // Anonymous access -> 401
    // -------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("Anonymous (unauthenticated) access")
    class AnonymousAccess {

        @Test
        @DisplayName("POST /api/v1/seller-applications without auth -> 401")
        void anonymousPostApplication_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/seller-applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validApplicationRequest())))
                    .andExpect(result -> assertThat(result.getResponse().getStatus())
                            .withFailMessage("anonymous POST status=%d body=%s",
                                    result.getResponse().getStatus(),
                                    result.getResponse().getContentAsString())
                            .isEqualTo(401));
        }

        @Test
        @DisplayName("GET /api/v1/admin/seller-applications without auth -> 401")
        void anonymousGetAdminList_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/admin/seller-applications"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus())
                            .withFailMessage("anonymous GET status=%d body=%s",
                                    result.getResponse().getStatus(),
                                    result.getResponse().getContentAsString())
                            .isEqualTo(401));
        }
    }

    // -------------------------------------------------------------------------------------------
    // BUYER access
    // -------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("BUYER role access")
    class BuyerRoleAccess {

        @Test
        @DisplayName("BUYER calling admin list -> 403")
        void buyerGetAdminList_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/admin/seller-applications").with(as(buyer())))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(403));
        }

        @Test
        @DisplayName("BUYER calling approve -> 403")
        void buyerApproveApplication_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/admin/seller-applications/1/approve").with(as(buyer())))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(403));
        }

        @Test
        @DisplayName("BUYER calling reject -> 403")
        void buyerRejectApplication_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/admin/seller-applications/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"rejectionReason\":\"no\"}")
                            .with(as(buyer())))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(403));
        }

        @Test
        @DisplayName("BUYER submitting an application -> 201")
        void buyerSubmitApplication_returns201() throws Exception {
            when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
            when(sellerApplicationService.submit(eq(10L), any())).thenReturn(
                    SellerApplicationResponse.builder()
                            .id(1L).shopName("My Shop").status(SellerApplicationStatus.PENDING).build());

            mockMvc.perform(post("/api/v1/seller-applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validApplicationRequest()))
                            .with(as(buyer())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(201);
                        assertThat(result.getResponse().getContentAsString()).contains("\"status\":\"PENDING\"");
                    });
        }
    }

    // -------------------------------------------------------------------------------------------
    // SELLER access
    // -------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("SELLER role access")
    class SellerRoleAccess {

        @Test
        @DisplayName("SELLER-only user submitting an application -> 403 (hasRole('BUYER') fails)")
        void sellerOnlyCannotSubmitApplication_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/seller-applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validApplicationRequest()))
                            .with(as(seller())))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(403));
        }

        @Test
        @DisplayName("BUYER+SELLER submitting -> passes @PreAuthorize, stopped by the service guard")
        void buyerAndSellerSubmit_reachesServiceAndGetsUserAlreadySeller() throws Exception {
            // A promoted seller still holds BUYER, so @PreAuthorize lets them through. The
            // service guard is what must stop them - the annotation is not sufficient.
            when(currentUserProvider.getCurrentUserId()).thenReturn(12L);
            when(sellerApplicationService.submit(eq(12L), any()))
                    .thenThrow(new BusinessException(ErrorCode.USER_ALREADY_SELLER));

            mockMvc.perform(post("/api/v1/seller-applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validApplicationRequest()))
                            .with(as(buyerAndSeller())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(409);
                        assertThat(result.getResponse().getContentAsString())
                                .contains("\"errorCode\":\"USER_ALREADY_SELLER\"");
                    });
        }

        @Test
        @DisplayName("SELLER reading own profile -> 200")
        void sellerGetProfile_returns200() throws Exception {
            when(currentUserProvider.getCurrentUserId()).thenReturn(11L);
            when(sellerService.getMyProfile(11L)).thenReturn(
                    SellerProfileResponse.builder()
                            .id(5L).userId(11L).shopName("Seller Shop").slug("seller-shop")
                            .businessType(BusinessType.INDIVIDUAL).status(SellerStatus.ACTIVE).build());

            mockMvc.perform(get("/api/v1/seller/profile").with(as(seller())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(200);
                        assertThat(result.getResponse().getContentAsString())
                                .contains("\"shopName\":\"Seller Shop\"");
                    });
        }

        @Test
        @DisplayName("BUYER reading seller profile -> 403")
        void buyerGetProfile_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/seller/profile").with(as(buyer())))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(403));
        }
    }

    // -------------------------------------------------------------------------------------------
    // ADMIN access
    // -------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("ADMIN role access")
    class AdminRoleAccess {

        @Test
        @DisplayName("ADMIN listing applications -> 200 with PageResponse")
        void adminGetList_returns200WithPageResponse() throws Exception {
            when(sellerApplicationService.list(any(), any())).thenReturn(
                    new PageImpl<>(List.of(SellerApplicationAdminDetailResponse.builder()
                                    .id(1L).userId(10L).userEmail("buyer@test.com")
                                    .userFullName("Buyer Test").shopName("My Shop")
                                    .status(SellerApplicationStatus.PENDING).build()),
                            PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/api/v1/admin/seller-applications").with(as(admin())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(200);
                        assertThat(result.getResponse().getContentAsString()).contains("\"totalElements\":1");
                    });
        }

        @Test
        @DisplayName("ADMIN approving -> 200")
        void adminApproveApplication_returns200() throws Exception {
            when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
            when(sellerApplicationService.approve(99L, 1L)).thenReturn(
                    SellerApplicationAdminDetailResponse.builder()
                            .id(1L).userId(10L).status(SellerApplicationStatus.APPROVED)
                            .reviewedByUserId(99L).reviewedAt(LocalDateTime.now()).build());

            mockMvc.perform(post("/api/v1/admin/seller-applications/1/approve").with(as(admin())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(200);
                        assertThat(result.getResponse().getContentAsString())
                                .contains("\"status\":\"APPROVED\"");
                    });
        }

        @Test
        @DisplayName("ADMIN rejecting -> 200")
        void adminRejectApplication_returns200() throws Exception {
            RejectSellerApplicationRequest req = RejectSellerApplicationRequest.builder()
                    .rejectionReason("Shop name violates policy").build();
            when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
            when(sellerApplicationService.reject(eq(99L), eq(1L), any())).thenReturn(
                    SellerApplicationAdminDetailResponse.builder()
                            .id(1L).userId(10L).status(SellerApplicationStatus.REJECTED)
                            .rejectionReason("Shop name violates policy")
                            .reviewedByUserId(99L).reviewedAt(LocalDateTime.now()).build());

            mockMvc.perform(post("/api/v1/admin/seller-applications/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(as(admin())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(200);
                        assertThat(result.getResponse().getContentAsString())
                                .contains("\"rejectionReason\":\"Shop name violates policy\"");
                    });
        }

        @Test
        @DisplayName("ADMIN reading detail -> 200")
        void adminGetDetail_returns200() throws Exception {
            when(sellerApplicationService.getDetail(1L)).thenReturn(
                    SellerApplicationAdminDetailResponse.builder()
                            .id(1L).userId(10L).userEmail("buyer@test.com")
                            .userFullName("Buyer Test").shopName("My Shop")
                            .status(SellerApplicationStatus.PENDING).build());

            mockMvc.perform(get("/api/v1/admin/seller-applications/1").with(as(admin())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(200);
                        assertThat(result.getResponse().getContentAsString()).contains("\"id\":1");
                    });
        }
    }

    // -------------------------------------------------------------------------------------------
    // Business error propagation
    // -------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("Business errors propagate through GlobalExceptionHandler")
    class BusinessErrorHandling {

        @Test
        @DisplayName("BusinessException carries errorCode into the response body")
        void businessExceptionSurfacesErrorCode() throws Exception {
            when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
            when(sellerApplicationService.submit(eq(10L), any()))
                    .thenThrow(new BusinessException(ErrorCode.BUYER_ROLE_REQUIRED));

            mockMvc.perform(post("/api/v1/seller-applications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validApplicationRequest()))
                            .with(as(buyer())))
                    .andExpect(result -> {
                        assertThat(result.getResponse().getStatus()).isEqualTo(403);
                        assertThat(result.getResponse().getContentAsString())
                                .contains("\"errorCode\":\"BUYER_ROLE_REQUIRED\"");
                    });
        }
    }
}