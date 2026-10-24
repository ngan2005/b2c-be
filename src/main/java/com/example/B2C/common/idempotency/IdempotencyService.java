package com.example.B2C.common.idempotency;

import com.example.B2C.common.exception.IdempotencyConflictException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    @Qualifier("idempotencyObjectMapper")
    private final ObjectMapper objectMapper;

    public String hashRequest(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash idempotency request", e);
        }
    }

    /**
     * Backward-compatible overload. Replays the response into a {@code Map} (i.e.
     * {@code Object.class}). Use {@link #executeOrReplay(String, String, String,
     * Class, Supplier)} when you need a concrete DTO type on the replay path.
     */
    @SuppressWarnings("unchecked")
    public <T> T executeOrReplay(String scope, String key, String requestHash, Supplier<T> supplier) {
        return (T) executeOrReplay(scope, key, requestHash, Object.class, (Supplier<Object>) supplier);
    }

    /**
     * Execute the supplier once per (scope, key).
     * <ul>
     *   <li>If a DONE record exists with the same request hash, the stored payload
     *       is deserialized into {@code type} and returned without invoking the
     *       supplier (replay path).</li>
     *   <li>If a DONE record exists with a different hash,
     *       {@link IdempotencyConflictException} is thrown.</li>
     *   <li>If IN_PROGRESS, the conflict is surfaced immediately.</li>
     *   <li>If FAILED, the record is reset and the supplier is re-invoked.</li>
     * </ul>
     * Runs in its own transaction ({@code REQUIRES_NEW}) so the result and the
     * record commit together regardless of the caller's transaction state.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeOrReplay(String scope, String key, String requestHash,
                                 Class<T> type, Supplier<T> supplier) {
        if (key == null || key.isBlank()) {
            return supplier.get();
        }
        var existing = repository.findByScopeAndIdempotencyKey(scope, key);
        if (existing.isPresent()) {
            IdempotencyRecord rec = existing.get();
            if (rec.getStatus() == IdempotencyStatus.DONE) {
                if (!rec.getRequestHash().equals(requestHash)) {
                    throw new IdempotencyConflictException(
                            "Idempotency key '" + key + "' was used with a different request body");
                }
                log.info("Replaying idempotent response for scope={} key={} as {}",
                        scope, key, type.getSimpleName());
                return deserialize(rec.getResponsePayload(), type);
            }
            if (rec.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                throw new IdempotencyConflictException(
                        "A request with idempotency key '" + key + "' is already in progress");
            }
            // FAILED -> allow retry; reset below
        }

        IdempotencyRecord record = existing.orElseGet(() -> IdempotencyRecord.builder()
                .scope(scope)
                .idempotencyKey(key)
                .requestHash(requestHash)
                .status(IdempotencyStatus.IN_PROGRESS)
                .build());

        record.setRequestHash(requestHash);
        record.setStatus(IdempotencyStatus.IN_PROGRESS);
        record.setCompletedAt(null);
        record.setResponsePayload(null);

        try {
            repository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyConflictException(
                    "Idempotency key '" + key + "' is being processed concurrently");
        }

        T result;
        try {
            result = supplier.get();
        } catch (RuntimeException ex) {
            record.setStatus(IdempotencyStatus.FAILED);
            record.setCompletedAt(LocalDateTime.now());
            repository.save(record);
            throw ex;
        }

        record.setStatus(IdempotencyStatus.DONE);
        record.setCompletedAt(LocalDateTime.now());
        record.setResponsePayload(serialize(result));
        repository.save(record);
        return result;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotency response", e);
        }
    }

    private <T> T deserialize(String payload, Class<T> type) {
        if (payload == null) {
            throw new IllegalStateException("Idempotency record has no response payload to replay");
        }
        try {
            if (type == Object.class) {
                @SuppressWarnings("unchecked")
                T cast = (T) objectMapper.readValue(payload, Object.class);
                return cast;
            }
            return objectMapper.readValue(payload, type);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to deserialize idempotent response into " + type.getName(), e);
        }
    }
}
