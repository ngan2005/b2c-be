package com.example.B2C.modules.inventory;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.strategy.InventoryStrategy;
import com.example.B2C.modules.inventory.strategy.E0NoLockStrategy;
import com.example.B2C.modules.inventory.strategy.E1OptimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E2PessimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E3OptimisticRetryStrategy;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that the inventory strategies behave correctly under concurrent load.
 * The H2 mock profile is used; pessimistic lock tests will pass on H2 because Spring
 * routes {@code LockModeType.PESSIMISTIC_WRITE} to {@code SELECT ... FOR UPDATE}
 * which H2 supports in PostgreSQL compatibility mode.
 */
@SpringBootTest
@ActiveProfiles({"db-test"})
class InventoryStrategyConcurrencyTest {

    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private E0NoLockStrategy e0;
    @Autowired private E1OptimisticStrategy e1;
    @Autowired private E2PessimisticStrategy e2;
    @Autowired private E3OptimisticRetryStrategy e3;

    private Long variantId;

    @BeforeEach
    @Transactional
    void setup() {
        // Find or create a product+variant for the test. Creating a real chain
        // (User -> Seller -> Product -> ProductVariant) so the FK constraints are
        // satisfied.
        Role role = roleRepository.findAll().stream().findFirst().orElseGet(() ->
                roleRepository.save(Role.builder()
                        .code("CUSTOMER")
                        .name("Customer")
                        .build()));

        User user = userRepository.findAll().stream().findFirst().orElseGet(() ->
                userRepository.save(User.builder()
                        .email("test-" + UUID.randomUUID() + "@example.com")
                        .passwordHash("test")
                        .fullName("Test user")
                        .role(role)
                        .status(UserStatus.ACTIVE)
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .build()));

        Seller seller = sellerRepository.findByUserId(user.getId()).orElseGet(() ->
                sellerRepository.save(Seller.builder()
                        .user(user)
                        .shopName("Test shop " + UUID.randomUUID().toString().substring(0, 8))
                        .slug("test-shop-" + UUID.randomUUID().toString().substring(0, 8))
                        .businessType(BusinessType.INDIVIDUAL)
                        .status(SellerStatus.ACTIVE)
                        .build()));

        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Test cat " + UUID.randomUUID().toString().substring(0, 6))
                        .slug("test-cat-" + UUID.randomUUID().toString().substring(0, 6))
                        .isActive(true)
                        .build()));

        Product product = productRepository.findAll().stream().findFirst().orElseGet(() -> {
            Product p = Product.builder()
                    .seller(seller)
                    .category(category)
                    .name("Test product")
                    .slug("test-product-" + UUID.randomUUID().toString().substring(0, 8))
                    .status(ProductStatus.ACTIVE)
                    .build();
            return productRepository.save(p);
        });

        // Reset stock to 10 for the test variant. Re-load from DB first to get
        // the current version, then update. This avoids optimistic-lock conflicts
        // with the previous test's residual state.
        ProductVariant existing = variantRepository.findAll().stream()
                .filter(x -> x.getProduct() != null && x.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        ProductVariant v;
        if (existing == null) {
            v = variantRepository.saveAndFlush(ProductVariant.builder()
                    .product(product)
                    .sku("TEST-SKU-" + System.nanoTime())
                    .variantName("Test variant")
                    .price(BigDecimal.valueOf(100000))
                    .stockQuantity(10)
                    .reservedQuantity(0)
                    .soldCount(0)
                    .isActive(true)
                    .version(0L)
                    .build());
        } else {
            existing.setStockQuantity(10);
            existing.setReservedQuantity(0);
            existing.setSoldCount(0);
            v = variantRepository.saveAndFlush(existing);
        }
        variantId = v.getId();
    }

    @Test
    @DisplayName("E0 baseline: 20 concurrent reserves on stock=10 — expect OVERSELL")
    void e0ShouldOversell() throws Exception {
        runRace(e0, 20, 1);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        // E0 has no lock — successful reservations may exceed initial stock
        int reserved = after.getReservedQuantity() == null ? 0 : after.getReservedQuantity();
        int stock = after.getStockQuantity();
        log("E0", stock, reserved, stock + reserved);
        // We deliberately do not assert no-oversell; we only assert the test ran
        assertTrue(stock + reserved <= 20, "Should not exceed total attempts");
    }

    @Test
    @DisplayName("E1 optimistic: 20 concurrent reserves on stock=10 — no oversell")
    void e1ShouldNotOversell() throws Exception {
        runRace(e1, 20, 1);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        int reserved = after.getReservedQuantity() == null ? 0 : after.getReservedQuantity();
        log("E1", after.getStockQuantity(), reserved, after.getStockQuantity() + reserved);
        // No oversell: stock + reserved must equal initial stock (10)
        assertEquals(10, after.getStockQuantity() + reserved,
                "Total stock + reserved must equal initial stock (10) — no oversell, no lost updates");
        assertTrue(reserved <= 10, "Reserved count should be <= initial stock");
    }

    @Test
    @DisplayName("E2 pessimistic: 20 concurrent reserves on stock=10 — no oversell")
    void e2ShouldNotOversell() throws Exception {
        runRace(e2, 20, 1);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        int reserved = after.getReservedQuantity() == null ? 0 : after.getReservedQuantity();
        log("E2", after.getStockQuantity(), reserved, after.getStockQuantity() + reserved);
        assertEquals(10, after.getStockQuantity() + reserved,
                "Total stock + reserved must equal initial stock (10)");
        assertTrue(reserved <= 10);
    }

    @Test
    @DisplayName("E3 optimistic+retry: 20 concurrent reserves on stock=10 — no oversell")
    void e3ShouldNotOversell() throws Exception {
        runRace(e3, 20, 1);
        ProductVariant after = variantRepository.findById(variantId).orElseThrow();
        int reserved = after.getReservedQuantity() == null ? 0 : after.getReservedQuantity();
        log("E3", after.getStockQuantity(), reserved, after.getStockQuantity() + reserved);
        assertEquals(10, after.getStockQuantity() + reserved,
                "Total stock + reserved must equal initial stock (10)");
        assertTrue(reserved <= 10);
    }

    @Test
    @DisplayName("Out of stock throws OutOfStockException")
    void outOfStockThrows() {
        ProductVariant v = variantRepository.findById(variantId).orElseThrow();
        v.setStockQuantity(0);
        variantRepository.saveAndFlush(v);
        assertThrows(OutOfStockException.class,
                () -> e1.reserve(v, 1, 1L));
    }

    private void runRace(InventoryStrategy strategy, int concurrency, int qty) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        List<Throwable> errors = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ProductVariant v = variantRepository.findById(variantId).orElseThrow();
                    strategy.reserve(v, qty, null);
                    success.incrementAndGet();
                } catch (OutOfStockException ex) {
                    failed.incrementAndGet();
                } catch (Throwable ex) {
                    failed.incrementAndGet();
                    synchronized (errors) {
                        errors.add(ex);
                    }
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await(60, TimeUnit.SECONDS);
        pool.shutdownNow();
        if (!errors.isEmpty()) {
            System.out.println("First error: " + errors.get(0).getClass().getSimpleName()
                    + " - " + errors.get(0).getMessage());
        }
    }

    private static void log(String label, int stock, int reserved, int sum) {
        System.out.printf("[%s] final stock=%d reserved=%d sum=%d%n", label, stock, reserved, sum);
    }
}
