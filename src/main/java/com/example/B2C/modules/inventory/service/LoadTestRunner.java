package com.example.B2C.modules.inventory.service;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.entity.LoadTestResult;
import com.example.B2C.modules.inventory.repository.LoadTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestRunner {

    private final InventoryService inventoryService;
    private final ProductVariantRepository variantRepository;
    private final LoadTestResultRepository resultRepository;

    @Transactional
    public LoadTestResult runExperiment(String scenario, Long variantId, int concurrency, int perThread) {
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
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int j = 0; j < perThread; j++) {
                        long t0 = System.nanoTime();
                        try {
                            inventoryService.reserveForLoadTest(variantId, 1);
                            success.incrementAndGet();
                        } catch (OutOfStockException ex) {
                            failed.incrementAndGet();
                        } catch (Throwable ex) {
                            failed.incrementAndGet();
                            conflicts.incrementAndGet();
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
        ProductVariant finalV = variantRepository.findById(variantId).orElseThrow();
        int finalStock = finalV.getStockQuantity();
        int oversell = Math.max(0, success.get() - initialStock);

        Collections.sort(latencies);
        LoadTestResult result = LoadTestResult.builder()
                .scenario(scenario)
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
                        + ",strategy=" + inventoryService.currentStrategyId())
                .build();
        if (!errors.isEmpty() && errors.get(0) != null) {
            log.warn("Sample error during {}: {}", scenario, errors.get(0).getClass().getSimpleName());
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
