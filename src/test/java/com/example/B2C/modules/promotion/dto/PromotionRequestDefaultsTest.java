package com.example.B2C.modules.promotion.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that Jackson deserialization keeps the {@code @Builder.Default}
 * fallbacks on the promotion request DTOs.
 *
 * <p>The admin UI posts a partial payload (no {@code applyScope}, no
 * {@code limitPerUser}), and Lombok's {@code @Builder.Default} only guards
 * the builder — whether the no-args constructor used by Jackson also carries
 * the default is version-sensitive. If it does not, the {@code @NotNull}
 * constraints would reject a perfectly valid admin update with a 400.
 */
class PromotionRequestDefaultsTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Test
    @DisplayName("UpdatePromotionRequest keeps defaults for fields the admin form omits")
    void updateRequestKeepsDefaults() throws Exception {
        String json = """
                {
                  "code": "SUMMER2026",
                  "name": "Summer sale",
                  "discountType": "FIXED",
                  "discountValue": 50000,
                  "quantity": 100,
                  "startAt": "2026-06-01T00:00:00",
                  "endAt": "2026-08-31T23:59:59"
                }
                """;

        UpdatePromotionRequest req = mapper.readValue(json, UpdatePromotionRequest.class);

        assertThat(req.getApplyScope()).as("applyScope default").isNotNull();
        assertThat(req.getLimitPerUser()).as("limitPerUser default").isNotNull();
        assertThat(req.getMinOrderValue()).as("minOrderValue default").isNotNull();
    }

    @Test
    @DisplayName("CreatePromotionRequest keeps defaults for fields the admin form omits")
    void createRequestKeepsDefaults() throws Exception {
        String json = """
                {
                  "code": "SUMMER2026",
                  "name": "Summer sale",
                  "discountType": "FIXED",
                  "discountValue": 50000,
                  "quantity": 100,
                  "startAt": "2026-06-01T00:00:00",
                  "endAt": "2026-08-31T23:59:59"
                }
                """;

        CreatePromotionRequest req = mapper.readValue(json, CreatePromotionRequest.class);

        assertThat(req.getApplyScope()).as("applyScope default").isNotNull();
        assertThat(req.getLimitPerUser()).as("limitPerUser default").isNotNull();
        assertThat(req.getMinOrderValue()).as("minOrderValue default").isNotNull();
    }
}
