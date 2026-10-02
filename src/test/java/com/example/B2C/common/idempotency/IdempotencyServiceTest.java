package com.example.B2C.common.idempotency;

import com.example.B2C.common.exception.IdempotencyConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"db-test"})
class IdempotencyServiceTest {

    @Autowired private IdempotencyService service;
    @Autowired private IdempotencyRecordRepository repository;

    @Test
    void replaysResultOnSecondCall() {
        String key = "test-" + UUID.randomUUID();
        String hash = service.hashRequest("body-A");
        String first = service.executeOrReplay("scope", key, hash, () -> "RESULT");
        String second = service.executeOrReplay("scope", key, hash, () -> "OTHER");
        assertEquals("RESULT", first);
        assertEquals("RESULT", second);
    }

    @Test
    void rejectsDifferentRequestHash() {
        String key = "test-" + UUID.randomUUID();
        service.executeOrReplay("scope", key, service.hashRequest("A"), () -> "x");
        assertThrows(IdempotencyConflictException.class,
                () -> service.executeOrReplay("scope", key, service.hashRequest("B"), () -> "y"));
    }

    @Test
    void retriesAfterFailure() {
        String key = "test-" + UUID.randomUUID();
        AtomicInteger calls = new AtomicInteger();
        try {
            service.executeOrReplay("scope", key, service.hashRequest("R"), () -> {
                calls.incrementAndGet();
                throw new RuntimeException("boom");
            });
        } catch (RuntimeException ignored) {
            // expected
        }
        // Re-run with the same hash — should not throw IdempotencyConflictException
        String result = service.executeOrReplay("scope", key, service.hashRequest("R"), () -> "OK");
        assertEquals("OK", result);
    }
}
