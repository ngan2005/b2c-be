package com.example.B2C.modules.ai.service;

import com.example.B2C.modules.ai.dto.AiChatRequest;
import com.example.B2C.modules.ai.dto.AiChatResponse;

public interface AiChatService {

    AiChatResponse chatWithAssistant(AiChatRequest request);
}
