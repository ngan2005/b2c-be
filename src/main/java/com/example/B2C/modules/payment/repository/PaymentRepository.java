package com.example.B2C.modules.payment.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.payment.entity.Payment;
import com.example.B2C.modules.payment.entity.PaymentTransactionStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends BaseRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderIdAndTransactionId(Long orderId, String transactionId);

    List<Payment> findByStatus(PaymentTransactionStatus status);
}
