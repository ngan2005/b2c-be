package com.example.B2C.common.email.impl;

import com.example.B2C.common.email.EmailMessage;
import com.example.B2C.common.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConditionalOnProperty(
        name = "application.email.provider",
        havingValue = "logging",
        matchIfMissing = true
)
@Slf4j
public class LoggingEmailService implements EmailService {

    /** Catches http://, https:// links inside the email body. */
    private static final Pattern LINK_PATTERN = Pattern.compile("https?://\\S+");

    /** Dev-only outbox: written next to the working directory so developers can copy links easily. */
    private static final Path OUTBOX_PATH = Paths.get("dev-outbox.log");

    @Override
    public void send(EmailMessage message) {
        String body = message.body();
        log.info("[EMAIL:logging] to={} subject={}\n{}", message.to(), message.subject(), body);
        writeToOutbox(message, body);
    }

    private void writeToOutbox(EmailMessage message, String body) {
        try {
            StringBuilder entry = new StringBuilder();
            entry.append("---- ").append(Instant.now()).append(" ----\n");
            entry.append("To: ").append(message.to()).append('\n');
            entry.append("Subject: ").append(message.subject()).append('\n');
            entry.append("Body:\n").append(body).append('\n');
            Matcher m = LINK_PATTERN.matcher(body);
            if (m.find()) {
                entry.append("RESET_LINK: ").append(m.group()).append('\n');
            }
            entry.append('\n');
            Files.writeString(
                    OUTBOX_PATH,
                    entry.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            log.warn("[EMAIL:logging] failed to write dev-outbox.log: {}", e.getMessage());
        }
    }
}
