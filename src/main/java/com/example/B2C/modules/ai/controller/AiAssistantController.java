package com.example.B2C.modules.ai.controller;

import com.example.B2C.modules.ai.dto.AiChatRequest;
import com.example.B2C.modules.ai.dto.AiChatResponse;
import com.example.B2C.modules.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chatWithAssistant(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.chatWithAssistant(request);
        return ResponseEntity.ok(response);
    }
}
