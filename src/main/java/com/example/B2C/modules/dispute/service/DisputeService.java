package com.example.B2C.modules.dispute.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.dispute.dto.DisputeDto;
import com.example.B2C.modules.dispute.dto.OpenDisputeRequest;
import com.example.B2C.modules.dispute.dto.ResolveDisputeRequest;
import com.example.B2C.modules.dispute.entity.DisputeStatus;
import com.example.B2C.modules.dispute.entity.DisputeType;
import com.example.B2C.modules.dispute.entity.OrderDispute;
import com.example.B2C.modules.dispute.repository.OrderDisputeRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.realtime.AdminEventBroadcaster;
import com.example.B2C.realtime.event.DisputeOpenedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final OrderDisputeRepository disputeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AdminEventBroadcaster broadcaster;

    /** Allowed order statuses from which a buyer may open a dispute. */
    private static final List<OrderStatus> DISPUTABLE_STATUSES = List.of(
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED,
            OrderStatus.SHIPPING
    );

    @Transactional
    public DisputeDto openDispute(Long orderId, Long userId, OpenDisputeRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("You do not own this order");
        }
        if (!DISPUTABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalArgumentException(
                    "Disputes can only be opened on orders that are DELIVERED, COMPLETED, or SHIPPING");
        }
        if (Boolean.TRUE.equals(disputeRepository.existsByOrderIdAndStatusIn(
                orderId, List.of(DisputeStatus.OPEN, DisputeStatus.INVESTIGATING)))) {
            throw new IllegalArgumentException("An open or investigating dispute already exists for this order");
        }

        OrderDispute dispute = OrderDispute.builder()
                .orderId(orderId)
                .openedBy(userId)
                .disputeType(req.getDisputeType())
                .description(req.getDescription())
                .status(DisputeStatus.OPEN)
                .build();

        OrderDispute saved = disputeRepository.save(dispute);

        // Publish realtime event
        DisputeOpenedEvent event = new DisputeOpenedEvent(
                orderId,
                order.getOrderCode(),
                userId,
                req.getDisputeType().name(),
                req.getDescription(),
                Instant.now()
        );
        broadcaster.broadcastExternal(
                "/topic/admin/disputes",
                "DISPUTE_OPENED",
                java.util.Map.of(
                        "orderId", orderId,
                        "orderCode", order.getOrderCode(),
                        "buyerId", userId,
                        "disputeType", req.getDisputeType().name(),
                        "description", req.getDescription(),
                        "openedAt", saved.getCreatedAt().toString()
                ),
                Instant.now(),
                saved.getId()
        );

        return toDto(saved);
    }

    public PageResponse<DisputeDto> listForAdmin(DisputeStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderDispute> result;
        if (status != null) {
            result = disputeRepository.findByStatus(status, pageable);
        } else {
            result = disputeRepository.findAll(pageable);
        }
        return PageResponse.of(result.map(this::toDto));
    }

    public DisputeDto getById(Long id) {
        OrderDispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
        return toDto(dispute);
    }

    @Transactional
    public DisputeDto resolve(Long id, Long adminUserId, ResolveDisputeRequest req) {
        OrderDispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));

        // Only allow final resolution statuses
        if (req.getStatus() != DisputeStatus.RESOLVED_REFUND
                && req.getStatus() != DisputeStatus.RESOLVED_REJECT
                && req.getStatus() != DisputeStatus.RESOLVED_PARTIAL) {
            throw new IllegalArgumentException("Resolution status must be RESOLVED_REFUND, RESOLVED_REJECT, or RESOLVED_PARTIAL");
        }

        dispute.setStatus(req.getStatus());
        dispute.setResolutionNote(req.getResolutionNote());
        dispute.setRefundAmount(req.getRefundAmount());
        dispute.setResolvedBy(adminUserId);
        dispute.setResolvedAt(LocalDateTime.now());

        OrderDispute saved = disputeRepository.save(dispute);
        return toDto(saved);
    }

    public DisputeDto toDto(OrderDispute d) {
        String openedByEmail = null;
        String resolvedByEmail = null;
        String orderCode = null;

        try {
            User opener = userRepository.findById(d.getOpenedBy()).orElse(null);
            if (opener != null) openedByEmail = opener.getEmail();
        } catch (Exception ignored) {}

        if (d.getResolvedBy() != null) {
            try {
                User resolver = userRepository.findById(d.getResolvedBy()).orElse(null);
                if (resolver != null) resolvedByEmail = resolver.getEmail();
            } catch (Exception ignored) {}
        }

        try {
            Order order = orderRepository.findById(d.getOrderId()).orElse(null);
            if (order != null) orderCode = order.getOrderCode();
        } catch (Exception ignored) {}

        return DisputeDto.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .orderCode(orderCode)
                .openedBy(d.getOpenedBy())
                .openedByEmail(openedByEmail)
                .disputeType(d.getDisputeType())
                .description(d.getDescription())
                .status(d.getStatus())
                .resolutionNote(d.getResolutionNote())
                .refundAmount(d.getRefundAmount())
                .resolvedBy(d.getResolvedBy())
                .resolvedByEmail(resolvedByEmail)
                .resolvedAt(d.getResolvedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
