package com.example.B2C.modules.finance.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.finance.dto.SellerNotificationDto;
import com.example.B2C.modules.finance.entity.SellerNotification;
import com.example.B2C.modules.finance.repository.SellerNotificationRepository;
import com.example.B2C.modules.finance.service.SellerNotificationService;
import com.example.B2C.modules.seller.repository.SellerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller/notifications")
@RequiredArgsConstructor
@Tag(name = "Seller Notifications", description = "Notifications received by the authenticated seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerNotificationController {

    private final SellerNotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;
    private final SellerRepository sellerRepository;
    private final SellerNotificationRepository notificationRepository;

    @GetMapping("/_debug")
    @Operation(summary = "DEBUG: dump auth context and DB state for the current seller")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debug() {
        Long userId = currentUserProvider.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("authUserId", userId);

        var sellerOpt = sellerRepository.findByUserId(userId);
        sellerOpt.ifPresentOrElse(
                s -> {
                    result.put("sellerId", s.getId());
                    result.put("shopName", s.getShopName());
                    result.put("status", s.getStatus().name());
                    List<SellerNotification> rows = notificationRepository
                            .findBySellerIdOrderByCreatedAtDesc(s.getId(),
                                    org.springframework.data.domain.PageRequest.of(0, 10))
                            .getContent();
                    result.put("notifCount", rows.size());
                    result.put("notifLatest", rows.stream().map(n -> Map.of(
                            "id", n.getId(),
                            "title", n.getTitle(),
                            "isRead", n.getIsRead(),
                            "createdAt", n.getCreatedAt() == null ? null : n.getCreatedAt().toString()
                    )).toList());
                },
                () -> {
                    result.put("error", "No Seller profile for userId=" + userId);
                }
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "List my notifications", description = "Returns paginated notifications for the authenticated seller.")
    public ResponseEntity<ApiResponse<PageResponse<SellerNotificationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long sellerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.listForSeller(sellerId, page, size)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Returns the number of unread notifications for the badge.")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount() {
        Long sellerId = currentUserProvider.getCurrentUserId();
        long count = notificationService.getUnreadCount(sellerId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark one as read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        Long sellerId = currentUserProvider.getCurrentUserId();
        notificationService.markAsRead(sellerId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        Long sellerId = currentUserProvider.getCurrentUserId();
        notificationService.markAllRead(sellerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
