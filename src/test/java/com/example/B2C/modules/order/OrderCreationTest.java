package com.example.B2C.modules.order;

import com.example.B2C.modules.cart.entity.Cart;
import com.example.B2C.modules.cart.entity.CartItem;
import com.example.B2C.modules.cart.repository.CartItemRepository;
import com.example.B2C.modules.cart.repository.CartRepository;
import com.example.B2C.modules.catalog.entity.Category;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.entity.ProductStatus;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.CategoryRepository;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.order.service.OrderService;
import com.example.B2C.modules.seller.entity.BusinessType;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Address;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.AddressRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the post-fix invariants of OrderServiceImpl.createOrderForBuyer:
 *  - OrderItem rows are persisted with a non-null orderId
 *  - subtotal/total are persisted on the orders row in one INSERT
 *  - getOrderForBuyer returns items
 */
@SpringBootTest
@Transactional
class OrderCreationTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;

    private Long userId;
    private Long addressId;
    private Long cartItemAId;
    private Long cartItemBId;

    @BeforeEach
    @Transactional
    void setup() {
        Role role = roleRepository.findAll().stream().findFirst().orElseGet(() ->
                roleRepository.save(Role.builder()
                        .code("CUSTOMER")
                        .name("Customer")
                        .build()));
        // Always create a brand-new user for this test so each test owns its own
        // cart, address, and order rows.
        User user = userRepository.save(User.builder()
                .email("ord-" + UUID.randomUUID() + "@example.com")
                .passwordHash("x").fullName("Order user")
                .role(role).status(UserStatus.ACTIVE)
                .dateOfBirth(LocalDate.of(1995, 5, 5))
                .build());
        userId = user.getId();

        Address address = addressRepository.save(Address.builder()
                .user(user)
                .recipientName("Nguyen Van A")
                .phone("0900000000")
                .streetDetail("123 Le Loi")
                .fullAddress("123 Le Loi, Q1, HCM")
                .build());
        addressId = address.getId();

        // Use a separate seller user to avoid user_id collision on the cart
        // (the cart FK + unique constraint is per-user). The seller is
        // linked to a different user record.
        User sellerUser = userRepository.save(User.builder()
                .email("seller-" + UUID.randomUUID() + "@example.com")
                .passwordHash("x").fullName("Seller user")
                .role(role).status(UserStatus.ACTIVE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build());
        Seller seller = sellerRepository.save(Seller.builder()
                .user(sellerUser)
                .shopName("Order Shop " + UUID.randomUUID().toString().substring(0, 4))
                .slug("ord-shop-" + UUID.randomUUID().toString().substring(0, 4))
                .businessType(BusinessType.INDIVIDUAL)
                .status(SellerStatus.ACTIVE)
                .build());
        Category category = categoryRepository.save(Category.builder()
                .name("Order cat " + UUID.randomUUID().toString().substring(0, 4))
                .slug("ord-cat-" + UUID.randomUUID().toString().substring(0, 4))
                .isActive(true)
                .build());
        Product product = productRepository.save(Product.builder()
                .seller(seller).category(category)
                .name("Order product")
                .slug("ord-prod-" + UUID.randomUUID().toString().substring(0, 6))
                .minPrice(BigDecimal.valueOf(100000))
                .maxPrice(BigDecimal.valueOf(200000))
                .status(ProductStatus.ACTIVE)
                .build());
        ProductVariant v1 = variantRepository.saveAndFlush(ProductVariant.builder()
                .product(product)
                .sku("ORD-A-" + System.nanoTime())
                .variantName("variant A")
                .price(BigDecimal.valueOf(100000))
                .salePrice(BigDecimal.valueOf(100000))
                .stockQuantity(10)
                .reservedQuantity(0)
                .soldCount(0)
                .isActive(true)
                .version(0L)
                .build());
        ProductVariant v2 = variantRepository.saveAndFlush(ProductVariant.builder()
                .product(product)
                .sku("ORD-B-" + System.nanoTime())
                .variantName("variant B")
                .price(BigDecimal.valueOf(50000))
                .salePrice(BigDecimal.valueOf(50000))
                .stockQuantity(10)
                .reservedQuantity(0)
                .soldCount(0)
                .isActive(true)
                .version(0L)
                .build());

        Cart cart = cartRepository.save(Cart.builder().user(user).build());
        CartItem a = cartItemRepository.save(CartItem.builder()
                .cart(cart).variant(v1)
                .priceSnapshot(BigDecimal.valueOf(100000))
                .quantity(2).isSelected(true).build());
        CartItem b = cartItemRepository.save(CartItem.builder()
                .cart(cart).variant(v2)
                .priceSnapshot(BigDecimal.valueOf(50000))
                .quantity(3).isSelected(true).build());
        cartItemAId = a.getId();
        cartItemBId = b.getId();
    }

    @Test
    @DisplayName("createOrderForBuyer: order has items with valid orderId, totals persisted correctly")
    void createOrder_persistsItemsAndTotals() {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .cartItemIds(List.of(cartItemAId, cartItemBId))
                .addressId(addressId)
                .paymentMethod("MOCK")
                .build();

        OrderDetailDto dto = orderService.createOrderForBuyer(userId, req, null);

        // DTO carries correct values
        assertNotNull(dto.getId());
        // subtotal = 2 * 100000 + 3 * 50000 = 350000
        assertEquals(0, dto.getSubtotal().compareTo(new BigDecimal("350000.00")),
                "subtotal should be 350000");
        // total = subtotal + 30000 shipping - 0 discount = 380000
        assertEquals(0, dto.getTotalAmount().compareTo(new BigDecimal("380000.00")),
                "total should be 380000");
        assertEquals(2, dto.getItems().size(), "two order items in the response DTO");

        // DB row carries the same totals
        var dbOrder = orderRepository.findById(dto.getId()).orElseThrow();
        assertEquals(0, dbOrder.getSubtotal().compareTo(new BigDecimal("350000.00")));
        assertEquals(0, dbOrder.getTotalAmount().compareTo(new BigDecimal("380000.00")));

        // OrderItem rows all have the orderId wired up
        List<OrderItem> dbItems = orderItemRepository.findByOrderId(dto.getId());
        assertEquals(2, dbItems.size());
        for (OrderItem item : dbItems) {
            assertNotNull(item.getOrderId(), "OrderItem.orderId must not be null");
            assertEquals(dto.getId(), item.getOrderId());
            assertNotNull(item.getVariantId());
            assertNotNull(item.getUnitPrice());
            assertNotNull(item.getTotalPrice());
        }
    }

    @Test
    @DisplayName("createOrderForBuyer: idempotency-key replays the same DTO without duplicating work")
    void createOrder_idempotencyKey_replays() {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .cartItemIds(List.of(cartItemAId, cartItemBId))
                .addressId(addressId)
                .paymentMethod("MOCK")
                .build();
        String key = "test-key-" + UUID.randomUUID();

        OrderDetailDto first = orderService.createOrderForBuyer(userId, req, key);
        OrderDetailDto second = orderService.createOrderForBuyer(userId, req, key);

        // Same order id, same DTO content
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getOrderCode(), second.getOrderCode());
        assertEquals(0, first.getTotalAmount().compareTo(second.getTotalAmount()));

        // No second row in the DB
        long sameBuyerOrders = orderRepository.findByBuyer(userId, null,
                org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent().stream()
                .filter(o -> key.equals(o.getIdempotencyKey()))
                .count();
        assertEquals(1, sameBuyerOrders, "idempotency key should produce exactly one order");
        assertTrue(orderItemRepository.findByOrderId(first.getId()).size() == 2);
    }
}
