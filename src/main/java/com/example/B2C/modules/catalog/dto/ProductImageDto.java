package com.example.B2C.modules.catalog.dto;

import com.example.B2C.modules.catalog.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {

    private Long id;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isThumbnail;

    /**
     * Maps a JPA entity to its DTO representation.
     */
    public static ProductImageDto from(ProductImage entity) {
        return ProductImageDto.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .altText(entity.getAltText())
                .sortOrder(entity.getSortOrder())
                .isThumbnail(entity.getIsThumbnail())
                .build();
    }
}
