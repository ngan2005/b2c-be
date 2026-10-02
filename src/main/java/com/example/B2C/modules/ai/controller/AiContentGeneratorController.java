package com.example.B2C.modules.ai.controller;

import com.example.B2C.modules.ai.dto.ProductDescriptionGenRequest;
import com.example.B2C.modules.ai.service.AiContentGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiContentGeneratorController {

    private final AiContentGeneratorService aiContentGeneratorService;

    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateProductDescription(@RequestBody ProductDescriptionGenRequest request) {
        String description = aiContentGeneratorService.generateProductDescription(request);
        return ResponseEntity.ok(Map.of("description", description));
    }
}
