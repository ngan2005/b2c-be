package com.example.B2C.common.idempotency;

import com.example.B2C.common.repository.BaseRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends BaseRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByScopeAndIdempotencyKey(String scope, String idempotencyKey);
}
