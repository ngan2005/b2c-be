package com.example.B2C.modules.stats.service;

import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerApplicationRepository;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.stats.dto.AdminStatsResponse;
import com.example.B2C.modules.stats.dto.RevenuePoint;
import com.example.B2C.modules.stats.dto.SellerStatsResponse;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatsService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;
    private final SellerApplicationRepository sellerApplicationRepository;
    private final UserRepository userRepository;

    public SellerStatsResponse getSellerStats(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Seller profile is not active");
        }

        Long sellerId = seller.getId();
        long totalProducts = productRepository.countBySellerIdAndDeletedAtIsNull(sellerId);
        long activeProducts = productRepository.countBySellerIdAndStatusAndDeletedAtIsNull(sellerId, ProductStatus.ACTIVE);
        long totalOrders = orderRepository.countBySellerId(sellerId);
        long pendingOrders = orderRepository.countBySellerIdAndStatus(sellerId, OrderStatus.PENDING);
        long lowStock = productVariantRepository.countLowStockForSeller(sellerId, ProductStatus.HIDDEN, LOW_STOCK_THRESHOLD);

        BigDecimal totalRevenue = nullSafe(orderRepository.sumRevenueBySeller(sellerId));
        LocalDateTime since30 = LocalDateTime.now().minusDays(30);
        BigDecimal revenueLast30 = nullSafe(orderRepository.sumRevenueBySellerSince(sellerId, since30));

        return SellerStatsResponse.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStock)
                .totalRevenue(totalRevenue)
                .revenueLast30Days(revenueLast30)
                .build();
    }

    public List<RevenuePoint> getSellerRevenue(Long userId, int days) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Seller profile is not active");
        }
        int safeDays = Math.max(1, Math.min(days, 365));
        LocalDateTime since = LocalDate.now().minusDays(safeDays - 1L).atStartOfDay();
        List<Object[]> rows = orderRepository.revenueByDayForSeller(seller.getId(), since);

        // Fill missing days with zero so the chart shows a continuous series.
        List<RevenuePoint> result = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(safeDays - 1L);
        int idx = 0;
        for (int i = 0; i < safeDays; i++) {
            LocalDate day = start.plusDays(i);
            BigDecimal revenue = BigDecimal.ZERO;
            if (idx < rows.size()) {
                Object[] row = rows.get(idx);
                LocalDate rowDay = toLocalDate(row[0]);
                if (rowDay != null && rowDay.equals(day)) {
                    revenue = toBigDecimal(row[1]);
                    idx++;
                }
            }
            result.add(RevenuePoint.builder().date(day).revenue(revenue).build());
        }
        return result;
    }

    public AdminStatsResponse getAdminStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long totalSellers = sellerRepository.count();
        long activeSellers = sellerRepository.countByStatus(SellerStatus.ACTIVE);
        long totalProducts = productRepository.countByDeletedAtIsNull();
        long activeProducts = productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.ACTIVE);
        long totalOrders = orderRepository.count();
        long pendingApps = sellerApplicationRepository.findByStatus(
                SellerApplicationStatus.PENDING,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        BigDecimal totalRevenue = nullSafe(orderRepository.sumRevenueAll());

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalSellers(totalSellers)
                .activeSellers(activeSellers)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalOrders(totalOrders)
                .pendingSellerApplications(pendingApps)
                .totalRevenue(totalRevenue)
                .build();
    }

    private BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate d) return d;
        if (o instanceof Date d) return d.toLocalDate();
        if (o instanceof java.time.LocalDateTime dt) return dt.toLocalDate();
        return null;
    }
}