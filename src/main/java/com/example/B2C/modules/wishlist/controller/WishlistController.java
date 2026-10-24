package com.example.B2C.modules.wishlist.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.wishlist.dto.WishlistItemDto;
import com.example.B2C.modules.wishlist.dto.WishlistToggleResponse;
import com.example.B2C.modules.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Authenticated user wishlist management")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List wishlist items for the current user")
    public ResponseEntity<ApiResponse<Page<WishlistItemDto>>> list(
        @PageableDefault(size = 20) Pageable pageable) {
        String email = currentUserEmail();
        return ResponseEntity.ok(ApiResponse.success(wishlistService.list(email, pageable)));
    }

    @PostMapping("/products/{productId}")
    @Operation(summary = "Toggle a product in/out of the wishlist")
    public ResponseEntity<ApiResponse<WishlistToggleResponse>> toggle(
        @PathVariable Long productId) {
        String email = currentUserEmail();
        WishlistToggleResponse result = wishlistService.toggle(email, productId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/products/{productId}")
    @Operation(summary = "Remove a product from the wishlist")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        String email = currentUserEmail();
        wishlistService.remove(email, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{productId}/check")
    @Operation(summary = "Check if a product is in the wishlist")
    public ResponseEntity<ApiResponse<WishlistToggleResponse>> check(@PathVariable Long productId) {
        String email = currentUserEmail();
        return ResponseEntity.ok(ApiResponse.success(WishlistToggleResponse.builder()
            .wishlisted(wishlistService.isWishlisted(email, productId))
            .productId(productId)
            .build()));
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new com.example.B2C.common.exception.UnauthorizedException("Authentication required");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        if (principal instanceof User u) {
            return u.getEmail();
        }
        return auth.getName();
    }
}
