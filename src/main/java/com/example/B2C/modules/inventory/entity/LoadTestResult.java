package com.example.B2C.modules.inventory.entity;

import com.example.B2C.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "load_test_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadTestResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario", nullable = false, length = 20)
    private String scenario;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "initial_stock", nullable = false)
    private Integer initialStock;

    @Column(name = "final_stock", nullable = false)
    private Integer finalStock;

    @Column(name = "oversell_count", nullable = false)
    @Builder.Default
    private Integer oversellCount = 0;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts;

    @Column(name = "successful_orders", nullable = false)
    @Builder.Default
    private Integer successfulOrders = 0;

    @Column(name = "failed_orders", nullable = false)
    @Builder.Default
    private Integer failedOrders = 0;

    @Column(name = "p50_latency_ms")
    private Integer p50LatencyMs;

    @Column(name = "p95_latency_ms")
    private Integer p95LatencyMs;

    @Column(name = "p99_latency_ms")
    private Integer p99LatencyMs;

    @Column(name = "avg_latency_ms")
    private Integer avgLatencyMs;

    @Column(name = "max_latency_ms")
    private Integer maxLatencyMs;

    @Column(name = "min_latency_ms")
    private Integer minLatencyMs;

    @Column(name = "throughput_rps")
    private Integer throughputRps;

    @Column(name = "conflict_count", nullable = false)
    @Builder.Default
    private Integer conflictCount = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
