-- V8: Load test result table for E0-E3 experiment metrics

CREATE TABLE load_test_result (
    id BIGSERIAL PRIMARY KEY,
    scenario VARCHAR(20) NOT NULL,
    variant_id BIGINT NOT NULL,
    initial_stock INT NOT NULL,
    final_stock INT NOT NULL,
    oversell_count INT NOT NULL DEFAULT 0,
    total_attempts INT NOT NULL,
    successful_orders INT NOT NULL DEFAULT 0,
    failed_orders INT NOT NULL DEFAULT 0,
    p50_latency_ms INT,
    p95_latency_ms INT,
    p99_latency_ms INT,
    avg_latency_ms INT,
    max_latency_ms INT,
    min_latency_ms INT,
    throughput_rps INT,
    conflict_count INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_load_test_scenario CHECK (scenario IN ('E0', 'E1', 'E2', 'E3'))
);

CREATE INDEX idx_load_test_scenario ON load_test_result(scenario);
CREATE INDEX idx_load_test_variant ON load_test_result(variant_id);
CREATE INDEX idx_load_test_started_at ON load_test_result(started_at);
