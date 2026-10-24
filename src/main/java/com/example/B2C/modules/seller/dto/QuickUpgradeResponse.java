// [SELLER-BUYER-UPGRADE]
package com.example.B2C.modules.seller.dto;

import com.example.B2C.modules.auth.dto.AuthResponse;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response returned after a successful quick-upgrade from BUYER to SELLER")
public class QuickUpgradeResponse {

    @Schema(description = "Seller profile ID", example = "5")
    private Long sellerId;

    @Schema(description = "Shop name", example = "My Awesome Shop")
    private String shopName;

    @Schema(description = "Seller account status", example = "ACTIVE")
    private String status;

    @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.sig")
    private String accessToken;

    @Schema(description = "New JWT refresh token", example = "eyJhbGciOiJIUzI1NiJ9.refresh.sig")
    private String refreshToken;

    @Schema(description = "User database ID", example = "1")
    private Long userId;

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User display name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Primary role after upgrade (always SELLER for quick-upgrade path)",
            example = "SELLER")
    private String role;

    @Schema(description = "All roles the user now holds", example = "[\"BUYER\",\"SELLER\"]")
    private List<String> roles;

    /**
     * Factory: builds a {@link QuickUpgradeResponse} from the upgraded {@link User},
     * the newly-created {@link Seller}, and the fresh {@link AuthResponse} tokens.
     */
    public static QuickUpgradeResponse from(User user, Seller seller, AuthResponse auth) {
        return QuickUpgradeResponse.builder()
                .sellerId(seller.getId())
                .shopName(seller.getShopName())
                .status(seller.getStatus().name())
                .accessToken(auth.getAccessToken())
                .refreshToken(auth.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(auth.getRole())
                .roles(auth.getRoles())
                .build();
    }
}
