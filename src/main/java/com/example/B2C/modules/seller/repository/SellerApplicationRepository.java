package com.example.B2C.modules.seller.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.seller.entity.SellerApplication;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerApplicationRepository extends BaseRepository<SellerApplication, Long> {

    Optional<SellerApplication> findByUserId(Long userId);

    Optional<SellerApplication> findByUserIdAndStatus(Long userId, SellerApplicationStatus status);

    boolean existsByUserIdAndStatusIn(Long userId, List<SellerApplicationStatus> statuses);

    Page<SellerApplication> findByStatus(SellerApplicationStatus status, Pageable pageable);

    Page<SellerApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sa FROM SellerApplication sa WHERE sa.id = :id")
    Optional<SellerApplication> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT sa FROM SellerApplication sa JOIN FETCH sa.user u WHERE sa.id = :id")
    Optional<SellerApplication> findByIdWithUser(@Param("id") Long id);
}
