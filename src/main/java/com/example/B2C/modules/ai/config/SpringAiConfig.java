package com.example.B2C.modules.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SpringAiConfig {

    /**
     * Real OpenAI ChatClient bean — only created when {@code spring.ai.openai.api-key}
     * is set. Without a key, this bean is absent and {@link #chatClientFallback}
     * ensures services can still inject a non-null {@code ChatClient} placeholder.
     *
     * <p>The mock below is intentionally a NO-OP ChatClient: every method returns
     * safe defaults. Service implementations detect "no API key" via
     * {@code ai.enabled} flag and switch to {@link MockAiResponder}.</p>
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
            name = "spring.ai.openai.api-key", matchIfMissing = false)
    public ChatClient openAiChatClient(ChatClient.Builder builder) {
        log.info("[AI] OpenAI ChatClient bean created (REAL mode).");
        return builder.build();
    }

    @Bean
    public org.springframework.boot.ApplicationRunner aiStartupBanner(
            @Value("${spring.ai.openai.api-key:}") String apiKey) {
        return args -> {
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("[AI] ============================================================");
                log.warn("[AI]  spring.ai.openai.api-key is EMPTY.");
                log.warn("[AI]  AI endpoints will return MOCK responses.");
                log.warn("[AI]  Set OPENAI_API_KEY env var to enable real OpenAI.");
                log.warn("[AI] ============================================================");
            } else {
                log.info("[AI] OpenAI API key detected (length={}). Using real AI.", apiKey.length());
            }
        };
    }
}
