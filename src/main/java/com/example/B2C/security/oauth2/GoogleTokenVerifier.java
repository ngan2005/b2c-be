package com.example.B2C.security.oauth2;

import com.example.B2C.common.exception.UnauthorizedException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Xác thực Google ID Token nhận được từ Mobile App (Android/iOS) hoặc Single-Page Apps.
 * Sử dụng endpoint https://oauth2.googleapis.com/tokeninfo của Google để verify chữ ký và hạn dùng.
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    private final RestClient restClient;
    private final String expectedClientId;

    public record GoogleUserPayload(
            String providerUserId,
            String email,
            String fullName,
            String avatarUrl
    ) {}

    public GoogleTokenVerifier(
            @Value("${spring.security.oauth2.client.registration.google.client-id:disabled}") String clientId
    ) {
        this.expectedClientId = clientId;
        this.restClient = RestClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .build();
    }

    public GoogleUserPayload verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Google ID Token không được để trống");
        }

        try {
            GoogleTokenInfo info = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tokeninfo")
                            .queryParam("id_token", idToken.trim())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        log.warn("Google tokeninfo trả về HTTP status: {}", resp.getStatusCode());
                        throw new UnauthorizedException("Xác thực Google ID Token thất bại (token không hợp lệ hoặc đã hết hạn)");
                    })
                    .body(GoogleTokenInfo.class);

            if (info == null || info.getEmail() == null || info.getSub() == null) {
                throw new UnauthorizedException("Không tìm thấy thông tin email/sub từ Google ID Token");
            }

            // Kiểm tra audience nếu client-id được cấu hình hợp lệ
            if (expectedClientId != null
                    && !expectedClientId.isBlank()
                    && !"disabled".equalsIgnoreCase(expectedClientId.trim())) {
                boolean matchAud = expectedClientId.equalsIgnoreCase(info.getAud());
                boolean matchAzp = expectedClientId.equalsIgnoreCase(info.getAzp());
                if (!matchAud && !matchAzp) {
                    log.warn("Audience mismatch: expected={}, token aud={}, azp={}",
                            expectedClientId, info.getAud(), info.getAzp());
                    throw new UnauthorizedException("Google ID Token không khớp với cấu hình Client ID của ứng dụng");
                }
            }

            String fullName = info.getName();
            if (fullName == null || fullName.isBlank()) {
                String given = info.getGivenName() != null ? info.getGivenName() : "";
                String family = info.getFamilyName() != null ? info.getFamilyName() : "";
                fullName = (given + " " + family).trim();
                if (fullName.isEmpty()) {
                    fullName = info.getEmail();
                }
            }

            return new GoogleUserPayload(
                    info.getSub(),
                    info.getEmail(),
                    fullName,
                    info.getPicture()
            );
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Lỗi khi kết nối đến Google OAuth2 API để verify token", ex);
            throw new UnauthorizedException("Không thể xác thực Google ID Token: " + ex.getMessage());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleTokenInfo {
        private String iss;
        private String sub;
        private String azp;
        private String aud;
        private String email;
        @JsonProperty("email_verified")
        private String emailVerified;
        private String name;
        private String picture;
        @JsonProperty("given_name")
        private String givenName;
        @JsonProperty("family_name")
        private String familyName;
        private String exp;
        @JsonProperty("error_description")
        private String errorDescription;
    }
}

