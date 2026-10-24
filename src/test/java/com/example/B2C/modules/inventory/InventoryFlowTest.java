package com.example.B2C.modules.inventory;

import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.service.InventoryService;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the inventory invariant:
 *   stockQuantity + reservedQuantity + soldCount = initial physical stock
 * for the three key lifecycles used by the order pipeline.
 */
@SpringBootTest
@Transactional
class InventoryFlowTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    private Long variantId;

    @BeforeEach
    @Transactional
    void setup() {
        Role role = roleRepository.findAll().stream().findFirst().orElseGet(() ->
                roleRepository.save(Role.builder()
                        .code("CUSTOMER")
                        .name("Customer")
                        .build()));
        User user = userRepository.findAll().stream().findFirst().orElseGet(() ->
                userRepository.save(User.builder()
                        .email("flow-" + UUID.randomUUID() + "@example.com")
                        .passwordHash("x").fullName("flow user")
                        .role(role).status(UserStatus.ACTIVE)
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .build()));
        Seller seller = sellerRepository.findByUserId(user.getId()).orElseGet(() ->
                sellerRepository.save(Seller.builder()
                        .user(user)
                        .shopName("Flow Shop " + UUID.randomUUID().toString().substring(0, 6))
                        .slug("flow-" + UUID.randomUUID().toString().substring(0, 6))
                        .businessType(BusinessType.INDIVIDUAL)
                        .status(SellerStatus.ACTIVE)
                        .build()));
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Flow cat " + UUID.randomUUID().toString().substring(0, 4))
                        .slug("flow-cat-" + UUID.randomUUID().toString().substring(0, 4))
                        .isActive(true)
                        .build()));
        Product product = productRepository.findAll().stream().findFirst().orElseGet(() ->
                productRepository.save(Product.builder()
                        .seller(seller).category(category)
                        .name("Flow product")
                        .slug("flow-prod-" + UUID.randomUUID().toString().substring(0, 6))
                        .status(ProductStatus.ACTIVE)
                        .build()));

        ProductVariant v = variantRepository.saveAndFlush(ProductVariant.builder()
                .product(product)
                .sku("FLOW-" + System.nanoTime())
                .variantName("Flow variant")
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(10)
                .reservedQuantity(0)
                .soldCount(0)
                .isActive(true)
                .version(0L)
                .build());
        variantId = v.getId();
    }

    @Test
    @DisplayName("reserve + release: stock returns to original, reserved drops to 0")
    void reserveThenRelease_returnsStockAndReserved() {
        inventoryService.reserve(variantId, 3, null);
        ProductVariant mid = variantRepository.findById(variantId).orElseThrow();
        assertEquals(7, mid.getStockQuantity(), "stock decremented by 3");
        assertEquals(3, mid.getReservedQuantity(), "reserved incremented by 3");

        inventoryService.release(variantId, 3);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        assertEquals(10, after.getStockQuantity(), "stock restored to initial");
        assertEquals(0, after.getReservedQuantity(), "reserved dropped back to 0");
        assertEquals(0, after.getSoldCount(), "sold unchanged");
    }

    @Test
    @DisplayName("reserve + confirm: reserved drops to 0, soldCount grows, stock stays decremented")
    void reserveThenConfirm_movesToSold() {
        inventoryService.reserve(variantId, 4, null);
        inventoryService.confirm(variantId, 4);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        assertEquals(6, after.getStockQuantity(), "stock stays decremented after confirm");
        assertEquals(0, after.getReservedQuantity(), "reserved dropped to 0 after confirm");
        assertEquals(4, after.getSoldCount(), "sold grew by reserved qty");
        // invariant: stock + reserved + sold = 10
        assertEquals(10,
                after.getStockQuantity() + after.getReservedQuantity() + after.getSoldCount());
    }

    @Test
    @DisplayName("reserve + confirm + cancel: stock returns, sold drops — no inflation")
    void reserveConfirmCancel_returnsFromSold() {
        inventoryService.reserve(variantId, 5, null);
        inventoryService.confirm(variantId, 5);
        // Simulate the cancel-after-confirm path. InventoryPort.release() will
        // dispatch to releaseSold because reserved == 0 and sold >= qty.
        inventoryService.release(variantId, 5);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        assertEquals(10, after.getStockQuantity(), "stock restored after cancel-from-sold");
        assertEquals(0, after.getReservedQuantity());
        assertEquals(0, after.getSoldCount(), "sold dropped back to 0");
        // invariant
        assertEquals(10,
                after.getStockQuantity() + after.getReservedQuantity() + after.getSoldCount());
    }

    @Test
    @DisplayName("releaseReserved + releaseSold split: explicit methods also satisfy invariant")
    void explicitReleaseMethods_preserveInvariant() {
        inventoryService.reserve(variantId, 6, null);
        // Cancel-before-payment
        inventoryService.releaseReserved(variantId, 2);
        ProductVariant mid = variantRepository.findById(variantId).orElseThrow();
        assertEquals(6, mid.getStockQuantity(), "stock recovered 2 units");
        assertEquals(4, mid.getReservedQuantity());

        // Pay
        inventoryService.confirm(variantId, 4);
        ProductVariant paid = variantRepository.findById(variantId).orElseThrow();
        assertEquals(6, paid.getStockQuantity());
        assertEquals(0, paid.getReservedQuantity());
        assertEquals(4, paid.getSoldCount());

        // Cancel-after-payment
        inventoryService.releaseSold(variantId, 4);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        assertEquals(10, after.getStockQuantity());
        assertEquals(0, after.getReservedQuantity());
        assertEquals(0, after.getSoldCount());
    }
}
