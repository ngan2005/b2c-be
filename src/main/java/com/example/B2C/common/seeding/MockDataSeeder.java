package com.example.B2C.common.seeding;

import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Minimal seed for the {@code mock} profile. With Flyway disabled, JPA's
 * {@code create-drop} builds the schema but leaves it empty, so we have to
 * populate the bare minimum needed for {@code /auth/login} and the buyer /
 * seller smoke flows: roles, demo buyer & seller users, the seller's shop
 * record, and a tiny category tree.
 */
@Component
@Profile("mock")
public class MockDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MockDataSeeder.class);

    // BCrypt cost=10 hash for "password123" (matches V2/V3 seed).
    private static final String PASSWORD_HASH = "$2a$10$cC5WBQWEpuZgMSmjFwV5fO/NKYtvoR6vHe8f8/yHGSFTmiEOanF72";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public MockDataSeeder(RoleRepository roleRepository,
                          UserRepository userRepository,
                          SellerRepository sellerRepository,
                          CategoryRepository categoryRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedUsers();
        seedSeller();
        seedCategories();
        log.info("Mock data seeding complete.");
    }

    private void seedRoles() {
        upsertRole(Role.CODE_BUYER, "Buyer", "Customer who purchases products");
        upsertRole(Role.CODE_SELLER, "Seller", "User who sells products");
        upsertRole(Role.CODE_ADMIN, "Admin", "System administrator");
    }

    private void upsertRole(String code, String name, String description) {
        if (roleRepository.findByCode(code).isPresent()) {
            return;
        }
        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        roleRepository.save(role);
    }

    private void seedUsers() {
        if (userRepository.findByEmail("demo.buyer@example.com").isEmpty()) {
            Role buyerRole = roleRepository.findByCode(Role.CODE_BUYER).orElseThrow();
            User buyer = new User();
            buyer.setEmail("demo.buyer@example.com");
            buyer.setPasswordHash(PASSWORD_HASH);
            buyer.setFullName("Demo Buyer");
            buyer.setPhone("0901234567");
            buyer.setRole(buyerRole);
            buyer.setStatus(com.example.B2C.modules.user.entity.UserStatus.ACTIVE);
            buyer.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(buyer);
            log.info("Seeded demo.buyer@example.com (password: password123)");
        }
        if (userRepository.findByEmail("demo.seller@example.com").isEmpty()) {
            Role sellerRole = roleRepository.findByCode(Role.CODE_SELLER).orElseThrow();
            User sellerUser = new User();
            sellerUser.setEmail("demo.seller@example.com");
            sellerUser.setPasswordHash(PASSWORD_HASH);
            sellerUser.setFullName("Demo Seller");
            sellerUser.setPhone("0909876543");
            sellerUser.setRole(sellerRole);
            sellerUser.setStatus(com.example.B2C.modules.user.entity.UserStatus.ACTIVE);
            sellerUser.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(sellerUser);
            log.info("Seeded demo.seller@example.com (password: password123)");
        }
    }

    private void seedSeller() {
        if (sellerRepository.findBySlug("demo-shop").isPresent()) {
            return;
        }
        userRepository.findByEmail("demo.seller@example.com").ifPresent(sellerUser -> {
            Seller seller = new Seller();
            seller.setUser(sellerUser);
            seller.setShopName("Demo Shop");
            seller.setSlug("demo-shop");
            seller.setDescription("Welcome to our demo shop!");
            seller.setBusinessType(BusinessType.INDIVIDUAL);
            seller.setStatus(com.example.B2C.modules.seller.entity.SellerStatus.ACTIVE);
            seller.setRatingAvg(new BigDecimal("4.50"));
            seller.setRatingCount(100);
            seller.setTotalProduct(0);
            sellerRepository.save(seller);
        });
    }

    private void seedCategories() {
        if (categoryRepository.findAll().stream().anyMatch(c -> "electronics".equals(c.getSlug()))) {
            return;
        }
        Category electronics = upsertCategory("Electronics", "electronics", null, 1, 1);
        Category fashion = upsertCategory("Fashion", "fashion", null, 1, 2);
        Category home = upsertCategory("Home & Living", "home-living", null, 1, 3);
        upsertCategory("Smartphones", "smartphones", electronics, 2, 1);
        upsertCategory("Laptops", "laptops", electronics, 2, 2);
        upsertCategory("Mens Clothing", "mens-clothing", fashion, 2, 1);
        upsertCategory("Furniture", "furniture", home, 2, 1);
    }

    private Category upsertCategory(String name, String slug, Category parent, int level, int sortOrder) {
        return categoryRepository.findAll().stream()
                .filter(c -> slug.equals(c.getSlug()))
                .findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    c.setSlug(slug);
                    c.setParent(parent);
                    c.setLevel(level);
                    c.setSortOrder(sortOrder);
                    c.setIsActive(true);
                    return categoryRepository.save(c);
                });
    }
}