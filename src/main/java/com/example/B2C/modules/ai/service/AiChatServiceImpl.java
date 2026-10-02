package com.example.B2C.modules.ai.service;

import com.example.B2C.modules.ai.dto.AiChatRequest;
import com.example.B2C.modules.ai.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final MockAiResponder mockResponder;
    private final org.springframework.ai.chat.client.ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Override
    public AiChatResponse chatWithAssistant(AiChatRequest request) {
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        if (!hasApiKey()) {
            // No OpenAI key configured — fall back to mock. We don't even inject the
            // ChatClient because Spring AI's auto-config will fail at .call() time
            // with 401 if we let it through.
            log.debug("[AI] OPENAI_API_KEY not set. Using mock responder.");
            return AiChatResponse.builder()
                    .reply(mockResponder.chatReply(request.getMessage()))
                    .conversationId(conversationId)
                    .build();
        }

        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            chatClient = chatClientBuilder.build();
        }

        String systemPrompt = "Ban la tro ly tu van mua sam thong minh cua san thuong mai dien tu B2C. " +
                "Hay tra loi nguoi dung mot cach lich su, hao hung va dua ra nhung goi y mua hang huu ich nhat.";

        String reply = chatClient.prompt()
                .system(systemPrompt)
                .user(request.getMessage())
                .call()
                .content();

        return AiChatResponse.builder()
                .reply(reply)
                .conversationId(conversationId)
                .build();
    }

    private boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
