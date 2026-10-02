package com.example.B2C.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDescriptionGenRequest {

    private String productName;
    private String categoryName;
    private String brand;
    private List<String> keyFeatures;
    private String targetAudience;
    private String tone; // e.g. "Professional", "Friendly", "SEO-optimized"
}
