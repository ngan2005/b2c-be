package com.example.B2C.modules.user.controller;

import com.example.B2C.common.exception.UnauthorizedException;
import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.user.dto.AddressDto;
import com.example.B2C.modules.user.dto.AddressRequest;
import com.example.B2C.modules.user.dto.ChangePasswordRequest;
import com.example.B2C.modules.user.dto.UpdateProfileRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;
import com.example.B2C.modules.user.service.AddressService;
import com.example.B2C.modules.user.service.UserService;
import com.example.B2C.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Authenticated user profile + address management")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile() {
        Long userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUserProfile(userId)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(userId, request)));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current user's password")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or wrong current password")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = currentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/addresses")
    @Operation(summary = "List all addresses of the current user (default first)")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getMyAddresses() {
        Long userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(addressService.getMyAddresses(userId)));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Create a new address (auto default if first address)")
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(@Valid @RequestBody AddressRequest request) {
        Long userId = currentUserId();
        AddressDto created = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(created));
    }

    @GetMapping("/addresses/{addressId}")
    @Operation(summary = "Get one of my addresses by id")
    public ResponseEntity<ApiResponse<AddressDto>> getAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        Long userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(addressService.getAddressById(userId, addressId)));
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update one of my addresses")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        Long userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success("Address updated", addressService.updateAddress(userId, addressId, request)));
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete one of my addresses")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long addressId) {
        Long userId = currentUserId();
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    @PutMapping("/addresses/{addressId}/default")
    @Operation(summary = "Mark an address as the default shipping address")
    public ResponseEntity<ApiResponse<AddressDto>> setDefaultAddress(@PathVariable Long addressId) {
        Long userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success("Default address updated", addressService.setDefaultAddress(userId, addressId)));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new UnauthorizedException("Authentication required");
        }
        return cud.getUser().getId();
    }
}
