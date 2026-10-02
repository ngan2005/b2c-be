package com.example.B2C.common.email.impl;

import com.example.B2C.common.email.EmailMessage;
import com.example.B2C.common.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "application.email.provider",
        havingValue = "logging",
        matchIfMissing = true
)
@Slf4j
public class LoggingEmailService implements EmailService {

    @Override
    public void send(EmailMessage message) {
        log.info("[EMAIL:logging] to={} subject={}\n{}", message.to(), message.subject(), message.body());
    }
}
