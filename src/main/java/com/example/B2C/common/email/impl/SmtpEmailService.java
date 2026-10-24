package com.example.B2C.common.email.impl;

import com.example.B2C.common.email.EmailMessage;
import com.example.B2C.common.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Real SMTP email sender, enabled by setting
 * {@code application.email.provider=smtp} in application-*.properties.
 *
 * <p>SMTP credentials are injected from {@code spring.mail.*} properties which
 * themselves are wired from environment variables (see application-dev.properties).
 * For Gmail, use an App Password (https://myaccount.google.com/apppasswords), not
 * the account password.
 */
@Service
@ConditionalOnProperty(
        name = "application.email.provider",
        havingValue = "smtp"
)
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailService(
            JavaMailSender mailSender,
            @Value("${application.email.from:${spring.mail.username}}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
        log.info("[EMAIL:smtp] enabled, from={}", from);
    }

    @Override
    public void send(EmailMessage message) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(message.to());
            msg.setSubject(message.subject());
            msg.setText(message.body());
            mailSender.send(msg);
            log.info("[EMAIL:smtp] sent to={} subject={}", message.to(), message.subject());
        } catch (Exception e) {
            // Do not let a mail failure roll back the caller's transaction (e.g.
            // password-reset flow). Log loudly so the user/operator notices.
            log.error("[EMAIL:smtp] FAILED to={} subject={} err={}",
                    message.to(), message.subject(), e.getMessage(), e);
        }
    }
}
