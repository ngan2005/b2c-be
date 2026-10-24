package com.example.B2C.modules.finance.service;

import com.example.B2C.modules.finance.dto.DailyRevenuePointDto;
import com.example.B2C.modules.finance.dto.FinanceOverviewDto;
import com.example.B2C.modules.finance.dto.TopCategoryDto;
import com.example.B2C.modules.finance.entity.CommissionRecord;
import com.example.B2C.modules.finance.entity.PayoutStatus;
import com.example.B2C.modules.finance.entity.SellerPayout;
import com.example.B2C.modules.finance.repository.CommissionRecordRepository;
import com.example.B2C.modules.finance.repository.SellerPayoutRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated finance statistics for the admin dashboard.
 * All queries are read-only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceStatsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderRepository orderRepository;
    private final CommissionRecordRepository commissionRecordRepository;
    private final SellerPayoutRepository sellerPayoutRepository;

    @Transactional(readOnly = true)
    public FinanceOverviewDto getOverview() {
        // GMV: sum of total_amount for COMPLETED orders
        BigDecimal gmv = orderRepository.sumRevenueAll();
        if (gmv == null) gmv = BigDecimal.ZERO;

        // Platform revenue: sum of commission_amount for COMPLETED orders
        // (via order status)
        List<CommissionRecord> allCommissions = commissionRecordRepository.findAll();
        BigDecimal platformRevenue = BigDecimal.ZERO;
        for (CommissionRecord cr : allCommissions) {
            Order o = cr.getOrder();
            if (o != null && o.getStatus() == OrderStatus.COMPLETED) {
                platformRevenue = platformRevenue.add(
                        cr.getCommissionAmount() != null ? cr.getCommissionAmount() : BigDecimal.ZERO);
            }
        }

        BigDecimal sellerNet = gmv.subtract(platformRevenue);

        // Payout totals
        BigDecimal pendingPayout = sellerPayoutRepository
                .sumNetAmountBySellerAndStatus(null, PayoutStatus.PENDING);
        BigDecimal paidPayout = sellerPayoutRepository
                .sumNetAmountBySellerAndStatus(null, PayoutStatus.PAID);
        if (pendingPayout == null) pendingPayout = BigDecimal.ZERO;
        if (paidPayout == null) paidPayout = BigDecimal.ZERO;

        // Counts
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long pendingPayoutsCount = sellerPayoutRepository.countBySellerIdAndStatus(null, PayoutStatus.PENDING);

        // Top 5 categories by GMV (from completed orders)
        List<TopCategoryDto> topCategories = computeTopCategories();

        return new FinanceOverviewDto(
                gmv.setScale(2, RoundingMode.HALF_UP),
                platformRevenue.setScale(2, RoundingMode.HALF_UP),
                sellerNet.setScale(2, RoundingMode.HALF_UP),
                pendingPayout.setScale(2, RoundingMode.HALF_UP),
                paidPayout.setScale(2, RoundingMode.HALF_UP),
                completedOrders,
                pendingPayoutsCount,
                topCategories);
    }

    @Transactional(readOnly = true)
    public List<DailyRevenuePointDto> getDailySeries(int days) {
        int d = Math.max(1, Math.min(days, 365));
        LocalDateTime from = LocalDateTime.now().minusDays(d).withHour(0).withMinute(0).withSecond(0);

        List<Object[]> rows = orderRepository.revenueByDayForAdmin(from, LocalDateTime.now().plusDays(1));

        // commission per day
        Map<String, BigDecimal> commissionByDay = new LinkedHashMap<>();
        List<CommissionRecord> allCommissions = commissionRecordRepository.findAll();
        for (CommissionRecord cr : allCommissions) {
            Order o = cr.getOrder();
            if (o != null && o.getStatus() == OrderStatus.COMPLETED && o.getCompletedAt() != null) {
                String day = o.getCompletedAt().format(DATE_FMT);
                commissionByDay.merge(day,
                        cr.getCommissionAmount() != null ? cr.getCommissionAmount() : BigDecimal.ZERO,
                        BigDecimal::add);
            }
        }

        // Build series from order revenue rows (may have gaps → fill with zeros)
        Map<String, BigDecimal> gmvByDay = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String dayStr = row[0] != null ? row[0].toString() : null;
            BigDecimal rev = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            if (dayStr != null) gmvByDay.put(dayStr, rev);
        }

        List<DailyRevenuePointDto> series = new ArrayList<>();
        LocalDate cursor = from.toLocalDate();
        LocalDate end = LocalDate.now();
        while (!cursor.isAfter(end)) {
            String key = cursor.format(DATE_FMT);
            BigDecimal gmv = gmvByDay.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal commission = commissionByDay.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal sellerNet = gmv.subtract(commission);
            series.add(new DailyRevenuePointDto(key, gmv, commission, sellerNet));
            cursor = cursor.plusDays(1);
        }

        return series;
    }

    private List<TopCategoryDto> computeTopCategories() {
        // Simple in-memory aggregation from all completed-order commission records.
        // In production this would be a single optimised JPQL query with a JOIN.
        Map<Long, BigDecimal> categoryGmv = new LinkedHashMap<>();
        Map<Long, String> categoryName = new LinkedHashMap<>();

        List<CommissionRecord> records = commissionRecordRepository.findAll();
        for (CommissionRecord cr : records) {
            Order o = cr.getOrder();
            if (o == null || o.getStatus() != OrderStatus.COMPLETED) continue;
            Long catId = cr.getCategory() != null ? cr.getCategory().getId() : null;
            if (catId == null) continue;
            String catName = cr.getCategory().getName();
            BigDecimal amount = cr.getOrderAmount() != null ? cr.getOrderAmount() : BigDecimal.ZERO;
            categoryGmv.merge(catId, amount, BigDecimal::add);
            categoryName.put(catId, catName);
        }

        return categoryGmv.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> new TopCategoryDto(e.getKey(), categoryName.get(e.getKey()), e.getValue()))
                .toList();
    }
}
