package com.example.B2C.modules.finance.repository;

import com.example.B2C.modules.finance.entity.SellerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerNotificationRepository extends JpaRepository<SellerNotification, Long> {

    Page<SellerNotification> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    long countBySellerIdAndIsReadFalse(Long sellerId);

    Optional<SellerNotification> findByIdAndSellerId(Long id, Long sellerId);
}
