package com.example.B2C.modules.ai.controller;

import com.example.B2C.modules.ai.dto.ProductDescriptionGenRequest;
import com.example.B2C.modules.ai.service.AiContentGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Content Generator", description = "AI-assisted content generation for sellers")
@SecurityRequirement(name = "bearerAuth")
public class AiContentGeneratorController {

    private final AiContentGeneratorService aiContentGeneratorService;

    @PostMapping("/generate-description")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Generate a product description using AI",
            description = "Seller provides product name and optional details; "
                    + "the AI generates an SEO-friendly, compelling product description. "
                    + "Supports Vietnamese and English."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Description generated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI service unavailable")
    })
    public ResponseEntity<Map<String, String>> generateProductDescription(
            @Valid @RequestBody ProductDescriptionGenRequest request) {
        String description = aiContentGeneratorService.generateProductDescription(request);
        return ResponseEntity.ok(Map.of("description", description));
    }
}
