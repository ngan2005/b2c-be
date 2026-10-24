package com.example.B2C.modules.finance.service;

import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.finance.dto.CommissionRuleDto;
import com.example.B2C.modules.finance.dto.CommissionRecordDto;
import com.example.B2C.modules.finance.dto.CommissionRuleDto;
import com.example.B2C.modules.finance.dto.CommissionRuleUpsertRequest;
import com.example.B2C.modules.finance.entity.CommissionRecord;
import com.example.B2C.modules.finance.entity.CommissionRule;
import com.example.B2C.modules.finance.repository.CommissionRecordRepository;
import com.example.B2C.modules.finance.repository.CommissionRuleRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import com.example.B2C.modules.finance.service.SellerNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommissionService {

    private final CommissionRuleRepository ruleRepository;
    private final CommissionRecordRepository recordRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SellerNotificationService sellerNotificationService;

    /**
     * Looks up the applicable rule for an order item (category-specific first, then
     * global fallback) and persists a commission snapshot. Idempotent — skips if a
     * record already exists for the given order_item_id.
     *
     * @throws IllegalStateException if no global default rule exists (should never happen
     *                              because the Flyway seed always creates one)
     */
    @Transactional
    public CommissionRecord snapshotForItem(OrderItem item, Order order) {
        if (recordRepository.existsByOrderItemId(item.getId())) {
            log.debug("Commission record already exists for order_item_id={}, skipping", item.getId());
            return recordRepository.findByOrderItemId(item.getId()).orElse(null);
        }

        Long categoryId = item.getProductId() != null ? resolveCategoryId(item.getProductId()) : null;
        CommissionRule rule = resolveRule(categoryId);

        BigDecimal orderAmount = item.getTotalPrice();
        BigDecimal rate = rule.getPercent();

        // Apply min-order-value threshold: skip rule entirely if below minimum
        BigDecimal minOrderValue = rule.getMinOrderValue() != null
                ? rule.getMinOrderValue()
                : BigDecimal.ZERO;
        if (minOrderValue.compareTo(BigDecimal.ZERO) > 0
                && orderAmount.compareTo(minOrderValue) < 0) {
            log.debug("Order item {} amount {} below min {} — skipping commission",
                    item.getId(), orderAmount, minOrderValue);
            return null;
        }

        // Compute commission: rate% of order_amount
        BigDecimal rawCommission = orderAmount
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Apply max-commission cap if configured
        BigDecimal maxCommission = rule.getMaxCommission();
        if (maxCommission != null && rawCommission.compareTo(maxCommission) > 0) {
            rawCommission = maxCommission;
        }

        BigDecimal sellerNet = orderAmount.subtract(rawCommission);

        Seller seller = sellerRepository.findById(order.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + order.getSellerId()));

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        CommissionRecord record = CommissionRecord.builder()
                .order(order)
                .orderItem(item)
                .seller(seller)
                .category(category)
                .rule(rule)
                .orderAmount(orderAmount)
                .commissionRate(rate)
                .commissionAmount(rawCommission)
                .sellerNet(sellerNet)
                .build();

        CommissionRecord saved = recordRepository.save(record);
        log.info("Commission snapshot created: order_item_id={}, rule_id={}, rate={}, commission={}",
                item.getId(), rule.getId(), rate, rawCommission);
        return saved;
    }

    /**
     * Resolve the applicable commission rule:
     * 1. Category-specific active rule (most specific wins)
     * 2. Global default rule (category_id IS NULL, active)
     * 3. Throw if neither exists
     */
    public CommissionRule resolveRule(Long categoryId) {
        if (categoryId != null) {
            var catRule = ruleRepository
                    .findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(categoryId);
            if (catRule.isPresent()) {
                return catRule.get();
            }
        }
        return ruleRepository.findFirstByCategoryIdIsNullAndIsActiveTrueOrderByEffectiveFromDesc()
                .orElseThrow(() -> new IllegalStateException(
                        "No active global commission rule found — expected seeded default"));
    }

    /**
     * List commission rules with optional active-only filter.
     */
    public PageResponse<CommissionRuleDto> listRules(
            Boolean activeOnly, int page, int size) {
        int s = Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(page, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommissionRule> rules;
        if (Boolean.TRUE.equals(activeOnly)) {
            rules = ruleRepository.findByIsActiveTrue(pageable);
        } else {
            rules = ruleRepository.findAllOrderByCreatedAtDesc(pageable);
        }
        return com.example.B2C.common.response.PageResponse.of(rules.map(CommissionRuleDto::from));
    }

    /**
     * Create a new commission rule.
     */
    @Transactional
    public CommissionRuleDto createRule(CommissionRuleUpsertRequest req) {
        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id: " + req.getCategoryId()));
        }

        CommissionRule rule = CommissionRule.builder()
                .category(category)
                .name(req.getName().trim())
                .percent(req.getPercent())
                .minOrderValue(req.getMinOrderValue() != null
                        ? req.getMinOrderValue() : BigDecimal.ZERO)
                .maxCommission(req.getMaxCommission())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .effectiveFrom(req.getEffectiveFrom() != null
                        ? req.getEffectiveFrom() : java.time.LocalDateTime.now())
                .effectiveTo(req.getEffectiveTo())
                .build();

        CommissionRule saved = ruleRepository.save(rule);
        log.info("Commission rule created: id={}, name={}", saved.getId(), saved.getName());
        sellerNotificationService.notifyAllSellers(saved, "CREATED");
        return CommissionRuleDto.from(saved);
    }

    /**
     * Update an existing commission rule.
     */
    @Transactional
    public CommissionRuleDto updateRule(Long id, CommissionRuleUpsertRequest req) {
        CommissionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionRule", "id: " + id));

        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id: " + req.getCategoryId()));
        }
        rule.setCategory(category);
        rule.setName(req.getName().trim());
        rule.setPercent(req.getPercent());
        rule.setMinOrderValue(req.getMinOrderValue() != null
                ? req.getMinOrderValue() : BigDecimal.ZERO);
        rule.setMaxCommission(req.getMaxCommission());
        if (req.getIsActive() != null) {
            rule.setIsActive(req.getIsActive());
        }
        if (req.getEffectiveFrom() != null) {
            rule.setEffectiveFrom(req.getEffectiveFrom());
        }
        rule.setEffectiveTo(req.getEffectiveTo());

        CommissionRule saved = ruleRepository.save(rule);
        log.info("Commission rule updated: id={}", saved.getId());
        sellerNotificationService.notifyAllSellers(saved, "UPDATED");
        return CommissionRuleDto.from(saved);
    }

    /**
     * Disable a rule (soft-disable, does not delete).
     */
    @Transactional
    public void disableRule(Long id) {
        CommissionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionRule", "id: " + id));
        rule.setIsActive(false);
        ruleRepository.save(rule);
        log.info("Commission rule disabled: id={}", id);
        sellerNotificationService.notifyAllSellers(rule, "DISABLED");
    }

    /**
     * Delete a rule — only allowed if no commission_record references it.
     */
    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("CommissionRule", "id: " + id);
        }
        if (ruleRepository.existsByRuleId(id)) {
            throw new ConflictException(
                    "Cannot delete commission rule " + id + " because it has associated commission records");
        }
        ruleRepository.deleteById(id);
        log.info("Commission rule deleted: id={}", id);
    }

    /**
     * Get a single rule by ID.
     */
    public CommissionRuleDto getRule(Long id) {
        CommissionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CommissionRule", "id: " + id));
        return CommissionRuleDto.from(rule);
    }

    private Long resolveCategoryId(Long productId) {
        return productRepository.findById(productId)
                .map(p -> p.getCategory() != null ? p.getCategory().getId() : null)
                .orElse(null);
    }
}
