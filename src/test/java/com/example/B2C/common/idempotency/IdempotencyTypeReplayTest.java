package com.example.B2C.common.idempotency;

import com.example.B2C.common.exception.IdempotencyConflictException;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the type-aware replay path. Before the fix, {@code inferType}
 * always returned {@code Object.class} so the replay deserialised into a
 * {@code Map} rather than a real DTO. After the fix, callers can pass the
 * target class and the replay returns a strongly-typed value.
 */
@SpringBootTest
class IdempotencyTypeReplayTest {

    @Autowired private IdempotencyService service;

    @Test
    void replayDeserialisesIntoProvidedType() {
        String key = "type-replay-" + UUID.randomUUID();
        String hash = service.hashRequest("body-X");

        OrderItemDto item = OrderItemDto.builder()
                .id(101L)
                .variantId(202L)
                .productName("Replay DTO product")
                .unitPrice(new BigDecimal("1234500"))
                .quantity(2)
                .totalPrice(new BigDecimal("2469000"))
                .isReviewed(false)
                .build();
        OrderDetailDto first = OrderDetailDto.builder()
                .id(7L)
                .orderCode("ORD-REPLAY")
                .buyerId(11L)
                .sellerId(22L)
                .subtotal(new BigDecimal("2469000"))
                .shippingFee(new BigDecimal("30000"))
                .totalAmount(new BigDecimal("2499000"))
                .paymentMethod("MOCK")
                .paymentStatus("UNPAID")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of(item))
                .build();

        OrderDetailDto result1 = service.executeOrReplay(
                "order_create", key, hash, OrderDetailDto.class, () -> first);

        AtomicInteger calls = new AtomicInteger();
        OrderDetailDto result2 = service.executeOrReplay(
                "order_create", key, hash, OrderDetailDto.class, () -> {
                    calls.incrementAndGet();
                    return first; // would be a fresh order if actually invoked
                });

        // Supplier invoked only once
        assertEquals(0, calls.get(), "supplier should not be invoked on replay");

        // Replay returns a real DTO, not a Map
        assertNotNull(result2);
        assertTrue(result2 instanceof OrderDetailDto,
                "replay should return an OrderDetailDto, not a Map; got "
                        + result2.getClass().getName());
        assertEquals(7L, result2.getId());
        assertEquals("ORD-REPLAY", result2.getOrderCode());
        assertEquals(0, result2.getTotalAmount().compareTo(new BigDecimal("2499000")));
        assertEquals(1, result2.getItems().size());
        assertEquals("Replay DTO product", result2.getItems().get(0).getProductName());
    }

    @Test
    void differentHashForSameKeyThrowsConflict() {
        String key = "type-replay-conflict-" + UUID.randomUUID();
        service.executeOrReplay("scope", key,
                service.hashRequest("A"), OrderDetailDto.class, () -> null);
        assertThrows(IdempotencyConflictException.class,
                () -> service.executeOrReplay("scope", key,
                        service.hashRequest("B"), OrderDetailDto.class, () -> null));
    }
}
