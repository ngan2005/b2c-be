package com.example.B2C.modules.ai.service;

import com.example.B2C.modules.ai.dto.ProductDescriptionGenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiContentGeneratorServiceImpl implements AiContentGeneratorService {

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final org.springframework.ai.chat.client.ChatClient.Builder chatClientBuilder;
    private final MockAiResponder mockResponder;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Override
    public String generateProductDescription(ProductDescriptionGenRequest request) {
        if (!hasApiKey()) {
            log.debug("[AI] OPENAI_API_KEY not set. Using mock responder for product description.");
            return mockResponder.productDescription(
                    request.getProductName(),
                    request.getCategoryName(),
                    request.getBrand(),
                    request.getKeyFeatures(),
                    request.getTargetAudience(),
                    request.getTone()
            );
        }

        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            chatClient = chatClientBuilder.build();
        }

        String systemPrompt = "Ban la chuyen gia viet bai ban hang (Copywriter E-commerce) chuan SEO. " +
                "Hay tu dong viet mo ta san pham chi tiet, thu hut va chuyen nghiep dua tren cac thong so nguoi dung cung cap.";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Ten san pham: ").append(request.getProductName()).append("\n");
        if (request.getCategoryName() != null) userPrompt.append("Danh muc: ").append(request.getCategoryName()).append("\n");
        if (request.getBrand() != null) userPrompt.append("Thuong hieu: ").append(request.getBrand()).append("\n");
        if (request.getKeyFeatures() != null && !request.getKeyFeatures().isEmpty()) {
            userPrompt.append("Tinh nang noi bat: ").append(String.join(", ", request.getKeyFeatures())).append("\n");
        }
        if (request.getTargetAudience() != null) userPrompt.append("Doi tuong khach hang: ").append(request.getTargetAudience()).append("\n");
        if (request.getTone() != null) userPrompt.append("Phong cach viet: ").append(request.getTone()).append("\n");

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt.toString())
                .call()
                .content();
    }

    private boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
