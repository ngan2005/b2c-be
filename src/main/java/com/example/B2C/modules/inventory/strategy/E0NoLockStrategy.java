package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * E0 — TRUE no-lock baseline. Uses a raw SQL UPDATE with a stock guard but
 * NO row lock and NO {@code @Version} check, so two concurrent transactions
 * can both read the same stock value, both pass the guard, and both commit
 * decrements — this is the race condition we want to demonstrate in the
 * load-test dashboard. Do not use in production.
 *
 * <p>The version field is intentionally ignored; the native UPDATE bypasses
 * Hibernate's optimistic-lock plumbing entirely.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class E0NoLockStrategy implements InventoryStrategy {

    private final ProductVariantRepository repository;
    private final JdbcTemplate jdbc;

    @Override
    public String id() {
        return "E0";
    }

    @Override
    public ProductVariantRepository repository() {
        return repository;
    }

    @Override
    public void reserve(Long variantId, int qty, Long orderId) {
        // Pre-flight: confirm row exists. Without this, a missing variant would
        // silently report 0 rows affected and look like an out-of-stock event.
        if (!repository.existsById(variantId)) {
            throw new OutOfStockException("Variant " + variantId + " not found");
        }

        // Raw UPDATE: no SELECT FOR UPDATE, no version check. Two threads can
        // both pass the stock >= qty predicate and both decrement.
        int affected = jdbc.update("""
                UPDATE product_variant
                   SET stock_quantity = stock_quantity - ?,
                       reserved_quantity = COALESCE(reserved_quantity, 0) + ?,
                       last_reserved_at = ?,
                       version = version + 1
                 WHERE id = ? AND stock_quantity >= ?
                """,
                qty, qty, Timestamp.valueOf(LocalDateTime.now()), variantId, qty);

        if (affected == 0) {
            // Either the row vanished (we checked above) or stock < qty.
            throw new OutOfStockException("Insufficient stock for variant " + variantId);
        }
    }
}
