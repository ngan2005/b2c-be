package com.example.B2C.modules.dispute.repository;

import com.example.B2C.modules.dispute.entity.DisputeStatus;
import com.example.B2C.modules.dispute.entity.OrderDispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDisputeRepository extends JpaRepository<OrderDispute, Long> {

    Page<OrderDispute> findByStatus(DisputeStatus status, Pageable pageable);

    Page<OrderDispute> findAll(Pageable pageable);

    boolean existsByOrderIdAndStatusIn(Long orderId, java.util.List<DisputeStatus> statuses);
}
