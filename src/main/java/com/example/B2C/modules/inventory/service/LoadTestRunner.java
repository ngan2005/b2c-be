package com.example.B2C.modules.inventory.service;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.entity.LoadTestResult;
import com.example.B2C.modules.inventory.repository.LoadTestResultRepository;
import com.example.B2C.modules.inventory.strategy.E0NoLockStrategy;
import com.example.B2C.modules.inventory.strategy.E1OptimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E2PessimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E3OptimisticRetryStrategy;
import com.example.B2C.modules.inventory.strategy.InventoryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestRunner {

    private final InventoryService inventoryService;
    private final ProductVariantRepository variantRepository;
    private final LoadTestResultRepository resultRepository;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbc;
    // All four strategy beans are injected so the runner can pick the algorithm
    // dictated by the *scenario* (E0/E1/E2/E3) at runtime, instead of being
    // pinned to the singleton `activeStrategy` configured at startup.
    private final E0NoLockStrategy e0;
    private final E1OptimisticStrategy e1;
    private final E2PessimisticStrategy e2;
    private final E3OptimisticRetryStrategy e3;

    /**
     * Maps the human-friendly scenario names sent by the Admin UI to the
     * canonical E0/E1/E2/E3 codes required by the {@code chk_load_test_scenario}
     * CHECK constraint. E0-E3 are passed through unchanged so older API
     * consumers keep working.
     */
    private static final Map<String, String> SCENARIO_ALIASES = buildAliasMap();

    private static Map<String, String> buildAliasMap() {
        Map<String, String> m = new HashMap<>();
        // UI labels
        m.put("NONE", "E0");
        m.put("NO_LOCK", "E0");
        m.put("NO-LOCK", "E0");
        m.put("OPTIMISTIC", "E1");
        m.put("PESSIMISTIC_WRITE", "E2");
        m.put("PESSIMISTIC", "E2");
        // canonical pass-through
        m.put("E0", "E0");
        m.put("E1", "E1");
        m.put("E2", "E2");
        m.put("E3", "E3");
        return Collections.unmodifiableMap(m);
    }

    /** Normalise a UI/API scenario string to a canonical E0-E3 code. */
    public static String normaliseScenario(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("scenario must not be null");
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        String code = SCENARIO_ALIASES.get(key);
        if (code == null) {
            throw new IllegalArgumentException(
                    "Unknown scenario '" + raw + "'. Expected one of "
                            + SCENARIO_ALIASES.keySet() + " (E0/E1/E2/E3 also accepted).");
        }
        return code;
    }

    private InventoryStrategy strategyFor(String normalisedCode) {
        return switch (normalisedCode) {
            case "E0" -> e0;
            case "E1" -> e1;
            case "E2" -> e2;
            case "E3" -> e3;
            default -> throw new IllegalStateException("Unreachable: " + normalisedCode);
        };
    }

    /**
     * Runs a concurrency experiment. The {@code scenario} parameter accepts
     * either canonical codes (E0/E1/E2/E3) or UI labels (NONE/OPTIMISTIC/
     * PESSIMISTIC_WRITE). It is normalised before being stored in
     * {@link LoadTestResult#getScenario()}.
     *
     * <p>Deliberately NOT {@code @Transactional}: the outer transaction would
     * pin a read snapshot that hides the per-thread {@code TransactionTemplate}
     * commits. We need the final stock read to see those commits so that
     * {@code oversellCount} and {@code finalStock} reflect real DB state.
     */
    public LoadTestResult runExperiment(String scenario, Long variantId, int concurrency, int perThread) {
        String normalised = normaliseScenario(scenario);
        InventoryStrategy strategy = strategyFor(normalised);

        ProductVariant v = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant " + variantId + " not found"));
        int initialStock = v.getStockQuantity();
        int totalAttempts = concurrency * perThread;

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>(totalAttempts));
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger conflicts = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        long startedAt = System.nanoTime();
        LocalDateTime startedWall = LocalDateTime.now();
        for (int i = 0; i < concurrency; i++) {
            final int threadIndex = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int j = 0; j < perThread; j++) {
                        long t0 = System.nanoTime();
                        try {
                            // Each reservation gets its own transaction via
                            // TransactionTemplate. This is critical: the worker
                            // threads are NOT participating in the outer
                            // @Transactional of runExperiment, so a self-call
                            // would skip the proxy and never start a tx.
                            final int idx = threadIndex * perThread + j;
                            transactionTemplate.executeWithoutResult(status -> {
                                strategy.reserve(variantId, 1, (long) idx);
                            });
                            success.incrementAndGet();
                        } catch (OutOfStockException ex) {
                            failed.incrementAndGet();
                        } catch (OptimisticLockingFailureException ex) {
                            failed.incrementAndGet();
                            conflicts.incrementAndGet();
                            errors.add(ex);
                        } catch (Throwable ex) {
                            failed.incrementAndGet();
                            errors.add(ex);
                        } finally {
                            latencies.add((System.nanoTime() - t0) / 1_000_000L);
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        try {
            ready.await(30, TimeUnit.SECONDS);
            start.countDown();
            done.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        pool.shutdownNow();

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
        // Read final stock via a NATIVE query inside a fresh REQUIRES_NEW tx so
        // we see all the worker-thread commits. Using JpaRepository#findById from
        // a non-transactional context can return a stale value because the
        // persistence context may cache the entity loaded at the top of the
        // method (or be tied to the open-in-view session if any).
        int finalStock = transactionTemplate.execute(status -> jdbc.queryForObject(
                "SELECT stock_quantity FROM product_variant WHERE id = ?",
                Integer.class, variantId));
        int oversell = Math.max(0, success.get() - initialStock);

        Collections.sort(latencies);
        LoadTestResult result = LoadTestResult.builder()
                .scenario(normalised)
                .variantId(variantId)
                .initialStock(initialStock)
                .finalStock(finalStock)
                .oversellCount(oversell)
                .totalAttempts(totalAttempts)
                .successfulOrders(success.get())
                .failedOrders(failed.get())
                .p50LatencyMs(percentile(latencies, 50))
                .p95LatencyMs(percentile(latencies, 95))
                .p99LatencyMs(percentile(latencies, 99))
                .avgLatencyMs(average(latencies))
                .minLatencyMs(latencies.isEmpty() ? 0 : latencies.get(0).intValue())
                .maxLatencyMs(latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1).intValue())
                .throughputRps(elapsedMs == 0 ? 0 : (int) (totalAttempts * 1000L / elapsedMs))
                .conflictCount(conflicts.get())
                .startedAt(startedWall)
                .endedAt(LocalDateTime.now())
                .notes("concurrency=" + concurrency + ",perThread=" + perThread
                        + ",strategy=" + normalised)
                .build();
        if (!errors.isEmpty() && errors.get(0) != null) {
            log.warn("Sample error during {}: {}", normalised, errors.get(0).getClass().getSimpleName());
        }
        return resultRepository.save(result);
    }

    private static int percentile(List<Long> sorted, int p) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1))).intValue();
    }

    private static int average(List<Long> values) {
        if (values.isEmpty()) return 0;
        long sum = 0;
        for (long v : values) sum += v;
        return (int) (sum / values.size());
    }
}
