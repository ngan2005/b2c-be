package com.example.B2C.modules.finance.repository;

import com.example.B2C.modules.finance.entity.PlatformWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PlatformWalletRepository extends JpaRepository<PlatformWallet, Long> {

    /** Returns the singleton wallet row. */
    Optional<PlatformWallet> findById(Long id);

    /**
     * Returns the singleton wallet row with a pessimistic write lock to prevent
     * concurrent credits/debits from producing an incorrect balance.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM PlatformWallet w WHERE w.id = :id")
    Optional<PlatformWallet> findByIdWithLock(@Param("id") Long id);
}
