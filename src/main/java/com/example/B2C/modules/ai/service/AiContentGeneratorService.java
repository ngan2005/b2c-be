package com.example.B2C.modules.ai.service;

import com.example.B2C.modules.ai.dto.ProductDescriptionGenRequest;

public interface AiContentGeneratorService {

    String generateProductDescription(ProductDescriptionGenRequest request);
}
