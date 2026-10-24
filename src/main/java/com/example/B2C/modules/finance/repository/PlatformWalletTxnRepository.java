package com.example.B2C.modules.finance.repository;

import com.example.B2C.modules.finance.entity.PlatformWalletTxn;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformWalletTxnRepository extends JpaRepository<PlatformWalletTxn, Long> {

    /**
     * Returns the most recent N transactions across all wallets.
     */
    @Query("SELECT t FROM PlatformWalletTxn t ORDER BY t.createdAt DESC")
    List<PlatformWalletTxn> findRecent(Pageable pageable);

    /**
     * Returns the most recent N transactions for a specific seller.
     */
    @Query("SELECT t FROM PlatformWalletTxn t WHERE t.refSellerId = :sellerId ORDER BY t.createdAt DESC")
    List<PlatformWalletTxn> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
}
