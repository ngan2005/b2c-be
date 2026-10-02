package com.example.B2C.modules.order.dto;

import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String variantName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Boolean isReviewed;

    public static OrderItemDto from(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .imageUrl(item.getImageUrl())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .discount(item.getDiscount())
                .totalPrice(item.getTotalPrice())
                .isReviewed(item.getIsReviewed())
                .build();
    }
}