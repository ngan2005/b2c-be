package com.example.B2C.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application provides an {@link ObjectMapper} bean for components that need to
 * serialise payloads outside the request/response cycle (e.g. IdempotencyService and
 * WebSocket broadcaster). The bean is exposed under the name
 * {@code idempotencyObjectMapper} to avoid clashes with the auto-configured
 * Spring MVC ObjectMapper.
 */
@Configuration
public class JacksonConfig {

    @Bean(name = "idempotencyObjectMapper")
    public ObjectMapper idempotencyObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
