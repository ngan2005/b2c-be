package com.example.B2C.modules.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Cart item with computed line total")
public class CartItemDto {
    @Schema(description = "Cart item ID", example = "1")
    private Long id;

    @Schema(description = "Product variant ID", example = "10")
    private Long variantId;

    @Schema(description = "Variant display name (e.g. 'Red / XL')", example = "Red / XL")
    private String variantName;

    @Schema(description = "Parent product name", example = "Premium Cotton T-Shirt")
    private String productName;

    @Schema(description = "Thumbnail image URL", example = "https://example.com/img.jpg")
    private String thumbnailUrl;

    @Schema(description = "Quantity in cart", example = "2")
    private Integer quantity;

    @Schema(description = "Price snapshot taken at time of adding (VND)", example = "150000.00")
    private BigDecimal priceSnapshot;

    @Schema(description = "quantity × priceSnapshot (VND)", example = "300000.00")
    private BigDecimal lineTotal;

    @Schema(description = "Whether the item is selected for checkout", example = "true")
    private Boolean isSelected;

    @Schema(description = "ISO-8601 timestamp when the item was added to cart", example = "2024-01-10T09:00:00")
    private LocalDateTime addedAt;
}
