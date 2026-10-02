package com.example.B2C.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 300)
    private String slug;

    @NotNull
    private Long categoryId;

    @Size(max = 150)
    private String brand;

    private String description;

    private String thumbnailUrl;

    @NotNull
    @Positive
    private BigDecimal minPrice;

    @NotNull
    @Positive
    private BigDecimal maxPrice;

    private Integer weightGram;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;

    /**
     * Optional desired status. Defaults to DRAFT when null.
     */
    private String status;
}