package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * E3 — Optimistic locking with bounded retry. Re-runs the optimistic reserve up to
 * {@code inventory.locking.retry.max-attempts} times with a small backoff before
 * giving up.
 *
 * Each retry attempt executes in an isolated transaction (REQUIRES_NEW) so that
 * an optimistic lock failure in attempt N rolls back only that attempt, allowing
 * attempt N+1 to execute cleanly against a fresh persistence context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class E3OptimisticRetryStrategy implements InventoryStrategy {

    private final ProductVariantRepository repository;
    private final PlatformTransactionManager transactionManager;

    @Value("${inventory.locking.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${inventory.locking.retry.backoff-ms:50}")
    private long backoffMs;

    @Override
    public String id() {
        return "E3";
    }

    @Override
    public ProductVariantRepository repository() {
        return repository;
    }

    @Override
    public void reserve(Long variantId, int qty, Long orderId) {
        RuntimeException lastEx = null;
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                final int currentAttempt = attempt;
                txTemplate.executeWithoutResult(status -> {
                    ProductVariant fresh = repository.findById(variantId)
                            .orElseThrow(() -> new OutOfStockException(
                                    "Variant " + variantId + " not found"));
                    int reserved = fresh.getReservedQuantity() == null ? 0 : fresh.getReservedQuantity();
                    int available = fresh.getStockQuantity() - reserved;
                    if (available < qty) {
                        throw new OutOfStockException("Insufficient stock for variant " + variantId);
                    }
                    fresh.setReservedQuantity(reserved + qty);
                    fresh.setLastReservedAt(LocalDateTime.now());
                    repository.saveAndFlush(fresh);
                    log.debug("Reserved variant {} qty {} successfully on attempt {}/{}",
                            variantId, qty, currentAttempt, maxAttempts);
                });
                return;
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                lastEx = (ex instanceof RuntimeException re) ? re : new ObjectOptimisticLockingFailureException(ProductVariant.class, variantId);
                log.debug("Optimistic conflict on variant {} attempt {}/{}",
                        variantId, attempt, maxAttempts);
                if (attempt < maxAttempts) {
                    sleep(backoffMs * attempt);
                }
            }
        }
        throw lastEx != null ? lastEx
                : new OutOfStockException("Failed to reserve variant " + variantId);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
