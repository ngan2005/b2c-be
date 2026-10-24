package com.example.B2C.modules.finance;

import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.finance.entity.CommissionRecord;
import com.example.B2C.modules.finance.entity.CommissionRule;
import com.example.B2C.modules.finance.repository.CommissionRecordRepository;
import com.example.B2C.modules.finance.repository.CommissionRuleRepository;
import com.example.B2C.modules.finance.service.CommissionService;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock private CommissionRuleRepository ruleRepository;
    @Mock private CommissionRecordRepository recordRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private CommissionService commissionService;

    private Seller dummySeller;
    private Category dummyCategory;
    private Product dummyProduct;
    private Order dummyOrder;
    private OrderItem dummyItem;

    @BeforeEach
    void setUp() {
        dummySeller = Seller.builder().id(7L).build();
        dummyCategory = Category.builder().id(3L).name("Electronics").build();
        dummyProduct = Product.builder().id(100L).category(dummyCategory).build();
        dummyOrder = Order.builder()
                .id(50L)
                .sellerId(7L)
                .status(com.example.B2C.modules.order.entity.OrderStatus.COMPLETED)
                .build();
        dummyItem = OrderItem.builder()
                .id(200L)
                .orderId(50L)
                .productId(100L)
                .totalPrice(BigDecimal.valueOf(150_000))
                .build();
    }

    @Nested
    @DisplayName("snapshotForItem()")
    class SnapshotForItem {

        @Test
        @DisplayName("Applies global default rule correctly: 5% of 150000 = 7500")
        void appliesGlobalDefaultRule() {
            when(recordRepository.existsByOrderItemId(200L)).thenReturn(false);
            when(productRepository.findById(100L)).thenReturn(Optional.of(dummyProduct));

            CommissionRule globalRule = CommissionRule.builder()
                    .id(1L)
                    .percent(BigDecimal.valueOf(5.00))
                    .minOrderValue(BigDecimal.ZERO)
                    .build();
            when(ruleRepository.findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(3L))
                    .thenReturn(Optional.empty());
            when(ruleRepository.findFirstByCategoryIdIsNullAndIsActiveTrueOrderByEffectiveFromDesc())
                    .thenReturn(Optional.of(globalRule));
            when(sellerRepository.findById(7L)).thenReturn(Optional.of(dummySeller));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(dummyCategory));
            when(recordRepository.save(any(CommissionRecord.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            commissionService.snapshotForItem(dummyItem, dummyOrder);

            ArgumentCaptor<CommissionRecord> captor = ArgumentCaptor.forClass(CommissionRecord.class);
            verify(recordRepository).save(captor.capture());
            CommissionRecord saved = captor.getValue();
            assertThat(saved.getCommissionRate()).isEqualByComparingTo("5.00");
            assertThat(saved.getCommissionAmount()).isEqualByComparingTo("7500.00"); // 5% of 150000
            assertThat(saved.getSellerNet()).isEqualByComparingTo("142500.00");        // 150000 - 7500
        }

        @Test
        @DisplayName("Category-specific rule beats global default when both exist")
        void categoryRuleBeatsGlobal() {
            when(recordRepository.existsByOrderItemId(200L)).thenReturn(false);
            when(productRepository.findById(100L)).thenReturn(Optional.of(dummyProduct));

            CommissionRule catRule = CommissionRule.builder()
                    .id(2L)
                    .percent(BigDecimal.valueOf(10.00))
                    .minOrderValue(BigDecimal.ZERO)
                    .build();
            when(ruleRepository.findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(3L))
                    .thenReturn(Optional.of(catRule));
            when(sellerRepository.findById(7L)).thenReturn(Optional.of(dummySeller));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(dummyCategory));
            when(recordRepository.save(any(CommissionRecord.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            commissionService.snapshotForItem(dummyItem, dummyOrder);

            ArgumentCaptor<CommissionRecord> captor = ArgumentCaptor.forClass(CommissionRecord.class);
            verify(recordRepository).save(captor.capture());
            assertThat(captor.getValue().getCommissionRate()).isEqualByComparingTo("10.00");
            assertThat(captor.getValue().getCommissionAmount()).isEqualByComparingTo("15000.00"); // 10%
        }

        @Test
        @DisplayName("Min order value threshold: skips commission when item total is below minimum")
        void skipsWhenBelowMinOrderValue() {
            when(recordRepository.existsByOrderItemId(200L)).thenReturn(false);
            when(productRepository.findById(100L)).thenReturn(Optional.of(dummyProduct));

            CommissionRule rule = CommissionRule.builder()
                    .id(3L)
                    .percent(BigDecimal.valueOf(5.00))
                    .minOrderValue(BigDecimal.valueOf(200_000)) // min higher than item total
                    .build();
            when(ruleRepository.findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(3L))
                    .thenReturn(Optional.of(rule));

            // Should return null (skipped) and NOT save a record
            var result = commissionService.snapshotForItem(dummyItem, dummyOrder);
            assertThat(result).isNull();
            verify(recordRepository, never()).save(any());
        }

        @Test
        @DisplayName("Max commission cap is respected: 10% of 500000 = 50000, capped at 30000")
        void maxCommissionCapRespected() {
            dummyItem = OrderItem.builder()
                    .id(201L)
                    .orderId(50L)
                    .productId(100L)
                    .totalPrice(BigDecimal.valueOf(500_000))
                    .build();
            when(recordRepository.existsByOrderItemId(201L)).thenReturn(false);
            when(productRepository.findById(100L)).thenReturn(Optional.of(dummyProduct));

            CommissionRule rule = CommissionRule.builder()
                    .id(4L)
                    .percent(BigDecimal.valueOf(10.00))
                    .minOrderValue(BigDecimal.ZERO)
                    .maxCommission(BigDecimal.valueOf(30_000))  // cap = 30000
                    .build();
            when(ruleRepository.findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(3L))
                    .thenReturn(Optional.of(rule));
            when(sellerRepository.findById(7L)).thenReturn(Optional.of(dummySeller));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(dummyCategory));
            when(recordRepository.save(any(CommissionRecord.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            commissionService.snapshotForItem(dummyItem, dummyOrder);

            ArgumentCaptor<CommissionRecord> captor = ArgumentCaptor.forClass(CommissionRecord.class);
            verify(recordRepository).save(captor.capture());
            CommissionRecord saved = captor.getValue();
            assertThat(saved.getCommissionRate()).isEqualByComparingTo("10.00");
            // raw = 500000 * 10% = 50000, but capped at 30000
            assertThat(saved.getCommissionAmount()).isEqualByComparingTo("30000.00");
            assertThat(saved.getSellerNet()).isEqualByComparingTo("470000.00"); // 500000 - 30000
        }

        @Test
        @DisplayName("Idempotent: skips when commission record already exists for order_item_id")
        void idempotentSkipsExisting() {
            when(recordRepository.existsByOrderItemId(200L)).thenReturn(true);
            CommissionRecord existing = CommissionRecord.builder().id(99L).build();
            when(recordRepository.findByOrderItemId(200L)).thenReturn(Optional.of(existing));

            var result = commissionService.snapshotForItem(dummyItem, dummyOrder);

            assertThat(result).isEqualTo(existing);
            verify(recordRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws IllegalStateException when no active global rule exists")
        void throwsWhenNoGlobalRule() {
            when(recordRepository.existsByOrderItemId(200L)).thenReturn(false);
            when(productRepository.findById(100L)).thenReturn(Optional.of(dummyProduct));
            when(ruleRepository.findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(3L))
                    .thenReturn(Optional.empty());
            when(ruleRepository.findFirstByCategoryIdIsNullAndIsActiveTrueOrderByEffectiveFromDesc())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> commissionService.snapshotForItem(dummyItem, dummyOrder))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No active global commission rule found");
        }
    }
}
