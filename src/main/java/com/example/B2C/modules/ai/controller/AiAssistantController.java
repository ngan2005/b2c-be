package com.example.B2C.modules.ai.controller;

import com.example.B2C.modules.ai.dto.AiChatRequest;
import com.example.B2C.modules.ai.dto.AiChatResponse;
import com.example.B2C.modules.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "AI-powered shopping assistant — natural language product Q&A and recommendations")
@SecurityRequirement(name = "bearerAuth")
public class AiAssistantController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(
            summary = "Chat with the AI shopping assistant",
            description = "Sends a natural language message to the AI assistant. "
                    + "The assistant can answer product questions, give recommendations, "
                    + "track order status, and help with returns or complaints. "
                    + "Returns a structured response with text and optional suggested actions."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI response returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI service unavailable")
    })
    public ResponseEntity<AiChatResponse> chatWithAssistant(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.chatWithAssistant(request);
        return ResponseEntity.ok(response);
    }
}
