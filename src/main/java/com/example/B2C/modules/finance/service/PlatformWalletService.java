package com.example.B2C.modules.finance.service;

import com.example.B2C.modules.finance.dto.PlatformWalletDto;
import com.example.B2C.modules.finance.dto.WalletTxnDto;
import com.example.B2C.modules.finance.entity.PlatformWallet;
import com.example.B2C.modules.finance.entity.PlatformWalletTxn;
import com.example.B2C.modules.finance.entity.TxnDirection;
import com.example.B2C.modules.finance.repository.PlatformWalletRepository;
import com.example.B2C.modules.finance.repository.PlatformWalletTxnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing the singleton platform wallet.
 *
 * <p>All mutating operations acquire a pessimistic write lock on the wallet row
 * to prevent concurrent credits/debits from producing incorrect balances.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformWalletService {

    private static final Long WALLET_ID = 1L;
    private static final int RECENT_TXN_LIMIT = 50;

    private final PlatformWalletRepository walletRepository;
    private final PlatformWalletTxnRepository txnRepository;

    // ---------------- read operations ----------------

    /**
     * Returns the current platform wallet snapshot including recent transactions.
     */
    @Transactional(readOnly = true)
    public PlatformWalletDto getWallet() {
        PlatformWallet wallet = getOrCreateWallet();
        List<PlatformWalletTxn> recent = txnRepository.findRecent(PageRequest.of(0, RECENT_TXN_LIMIT));
        return PlatformWalletDto.from(wallet, recent);
    }

    /**
     * Returns the current balance without the transaction history.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance() {
        return getOrCreateWallet().getBalance();
    }

    /**
     * Returns the last N transactions across all wallets.
     */
    @Transactional(readOnly = true)
    public List<WalletTxnDto> getRecentTransactions(int limit) {
        return txnRepository.findRecent(PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(WalletTxnDto::from)
                .toList();
    }

    // ---------------- mutating operations ----------------

    /**
     * Credits the platform wallet by the given amount and writes a transaction record.
     *
     * @param amount       positive amount to credit
     * @param reason       short reason code (COMMISSION, ADJUSTMENT, etc.)
     * @param refOrderId   optional order reference
     * @param refSellerId  optional seller reference
     * @param note         optional free-text note
     * @return the updated wallet
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public PlatformWallet credit(BigDecimal amount, String reason,
                                  Long refOrderId, Long refSellerId, String note) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        PlatformWallet wallet = walletRepository.findByIdWithLock(WALLET_ID)
                .orElseThrow(() -> new IllegalStateException("Platform wallet not initialised"));

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalCredited(wallet.getTotalCredited().add(amount));

        PlatformWalletTxn txn = PlatformWalletTxn.builder()
                .walletId(WALLET_ID)
                .direction(TxnDirection.CREDIT)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .reason(reason)
                .refOrderId(refOrderId)
                .refSellerId(refSellerId)
                .note(note)
                .build();

        walletRepository.save(wallet);
        txnRepository.save(txn);

        log.info("Wallet CREDIT +{} reason={} refOrderId={} newBalance={}",
                amount, reason, refOrderId, wallet.getBalance());
        return wallet;
    }

    /**
     * Debits the platform wallet by the given amount and writes a transaction record.
     *
     * @param amount       positive amount to debit
     * @param reason       short reason code (PAYOUT, ADJUSTMENT, etc.)
     * @param refOrderId   optional order reference
     * @param refSellerId  optional seller reference
     * @param note         optional free-text note
     * @return the updated wallet
     * @throws IllegalStateException if the balance is insufficient
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public PlatformWallet debit(BigDecimal amount, String reason,
                                Long refOrderId, Long refSellerId, String note) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        PlatformWallet wallet = walletRepository.findByIdWithLock(WALLET_ID)
                .orElseThrow(() -> new IllegalStateException("Platform wallet not initialised"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient wallet balance: have " + wallet.getBalance() + " but need " + amount);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalDebited(wallet.getTotalDebited().add(amount));

        PlatformWalletTxn txn = PlatformWalletTxn.builder()
                .walletId(WALLET_ID)
                .direction(TxnDirection.DEBIT)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .reason(reason)
                .refOrderId(refOrderId)
                .refSellerId(refSellerId)
                .note(note)
                .build();

        walletRepository.save(wallet);
        txnRepository.save(txn);

        log.info("Wallet DEBIT -{} reason={} refOrderId={} newBalance={}",
                amount, reason, refOrderId, wallet.getBalance());
        return wallet;
    }

    // ---------------- internal helpers ----------------

    /** Lazily creates the singleton wallet row if it does not exist. */
    private PlatformWallet getOrCreateWallet() {
        return walletRepository.findById(WALLET_ID)
                .orElseGet(() -> {
                    PlatformWallet w = PlatformWallet.builder()
                            .id(WALLET_ID)
                            .balance(BigDecimal.ZERO)
                            .totalCredited(BigDecimal.ZERO)
                            .totalDebited(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(w);
                });
    }
}
