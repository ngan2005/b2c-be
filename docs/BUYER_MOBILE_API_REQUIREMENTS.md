# TÀI LIỆU ĐẶC TẢ YÊU CẦU NÂNG CẤP BACKEND API (BUYER MOBILE APP)

> **Dự án:** B2C E-Commerce System  
> **Target:** Buyer Mobile Application (React Native / Flutter / iOS / Android)  
> **Backend Repository:** `b2c-be`  
> **Mục tiêu:** Bổ sung các trường dữ liệu và endpoint còn thiếu để hỗ trợ đầy đủ toàn bộ 24 màn hình & modal trên Mobile cho Người mua (Buyer).

---

> [!IMPORTANT]
> ### ĐIỀU KIỆN TIÊN QUYẾT: TUYỆT ĐỐI KHÔNG THAY ĐỔI DATABASE HIỆN TẠI (NO DB SCHEMA CHANGES)
> 1. **KHÔNG tạo migration Flyway mới** (không thêm file `V40__...sql`).
> 2. **KHÔNG thêm bảng mới, không thêm cột mới, không đổi kiểu dữ liệu** trong cơ sở dữ liệu hiện tại (Schema hiện tại đã đầy đủ từ `V1` đến `V39`).
> 3. **Tất cả các trường bổ sung trong DTO** phải được truy vấn/ánh xạ (mapping) thông qua quan hệ JPA sẵn có (`ProductVariant` -> `Product` -> `Seller`, `OrderItem`, `VariantOptionValue`...) hoặc tính toán trên tầng Service / In-Memory.

---

## MỤC LỤC
1. [Khớp nối tổng quan giữa 24 màn hình Mobile và API Backend](#1-khớp-nối-tổng-quan-giữa-24-màn-hình-mobile-và-api-backend)
2. [P0 - Nhóm yêu cầu cấp thiết (Block UI / Lỗi nghiệp vụ - Zero DB Changes)](#2-p0---nhóm-yêu-cầu-cấp-thiết-block-ui--lỗi-nghiệp-vụ---zero-db-changes)
   - [2.1. Bổ sung thông tin Shop và Tồn kho vào `CartItemDto`](#21-bổ-sung-thông-tin-shop-và-tồn-kho-vào-cartitemdto)
   - [2.2. Bổ sung tóm tắt sản phẩm đại diện vào `OrderSummaryDto`](#22-bổ-sung-tóm-tắt-sản-phẩm-đại-diện-vào-ordersummarydto)
3. [P1 - Nhóm chức năng quan trọng (Zero DB Changes)](#3-p1---nhóm-chức-năng-quan-trọng-zero-db-changes)
   - [3.1. API lấy chi tiết sản phẩm theo ID (`GET /api/v1/products/id/{id}`)](#31-api-lấy-chi-tiết-sản-phẩm-theo-id-get-apiv1productsidid)
   - [3.2. API lấy lại URL thanh toán cho đơn hàng (`POST /api/v1/buyer/orders/{id}/pay`)](#32-api-lấy-lại-url-thanh-toán-cho-đơn-hàng-post-apiv1buyerordersidpay)
   - [3.3. Bổ sung `optionValueIds` vào `ProductVariantDto`](#33-bổ-sung-optionvalueids-vào-productvariantdto)
4. [P2 - Nhóm tiện ích & Tối ưu hóa trải nghiệm (Zero DB Changes)](#4-p2---nhóm-tiện-ích--tối-ưu-hóa-trải-nghiệm-zero-db-changes)
   - [4.1. API tính trước đơn hàng (`POST /api/v1/buyer/orders/preview`)](#41-api-tính-trước-đơn-hàng-post-apiv1buyerorderspreview)
   - [4.2. API Đăng xuất (`POST /api/v1/auth/logout`) - Xử lý không cần DB](#42-api-đăng-xuất-post-apiv1authlogout---xử-lý-không-cần-db)
   - [4.3. API Xóa nhiều mục trong giỏ hàng (`DELETE /api/v1/cart/items`)](#43-api-xóa-nhiều-mục-trong-giỏ-hàng-delete-apiv1cartitems)
   - [4.4. Mở rộng bộ lọc Catalog (`brand`, `minRating`)](#44-mở-rộng-bộ-lọc-catalog-brand-minrating)
   - [4.5. Lọc đánh giá sản phẩm theo số sao (`rating`)](#45-lọc-đánh-giá-sản-phẩm-theo-số-sao-rating)
   - [4.6. API lấy nhanh số lượng món trong giỏ (`GET /api/v1/cart/count`)](#46-api-lấy-nhanh-số-lượng-món-trong-giỏ-get-apiv1cartcount)
5. [Lưu ý xử lý luồng VNPay trên Mobile (WebView Intercept)](#5-lưu-ý-xử-lý-luồng-vnpay-trên-mobile-webview-intercept)
6. [Bảng tổng hợp Checklist cho Backend Developer](#6-bảng-tổng-hợp-checklist-cho-backend-developer)

---

## 1. KHỚP NỐI TỔNG QUAN GIỮA 24 MÀN HÌNH MOBILE VÀ API BACKEND

| Nhóm chức năng | Màn hình Mobile | API Backend tương ứng | Tình trạng khớp nối |
| :--- | :--- | :--- | :--- |
| **1. Auth** | Màn 1: Đăng nhập | `POST /api/v1/auth/login` | ✅ Đã có sẵn 100% |
| | Màn 2: Đăng ký | `POST /api/v1/auth/register` | ✅ Đã có sẵn 100% |
| | Màn 3: Quên/Đổi MK | `POST /api/v1/auth/forgot-password`, `/reset-password` | ✅ Đã có sẵn 100% |
| | Đăng xuất | `POST /api/v1/auth/logout` | 🟡 Cần thêm endpoint (Không tạo DB table) |
| **2. Catalog** | Màn 4: Trang chủ | `GET /api/v1/products/featured`, `/categories/roots`, `/promotions` | ✅ Đã có sẵn |
| | Màn 5, 6: Search & Lọc | `GET /api/v1/products` | 🟡 Cần thêm param `brand`, `minRating` (cột đã có trong DB) |
| | Màn 7: Cây danh mục | `GET /api/v1/categories/tree` | ✅ Đã có sẵn 100% |
| **3. Product** | Màn 8: Chi tiết SP | `GET /api/v1/products/{slug}` & `GET /api/v1/products/id/{id}` | 🟡 Cần thêm route tìm theo ID (dùng `findById`) |
| | Màn 9: Chọn Variant | Dữ liệu `variants` và `options` trong Product Detail | 🟡 Cần thêm `optionValueIds` vào DTO (bảng đã có) |
| | Màn 10: Đánh giá SP | `GET /api/v1/products/{id}/reviews` | 🟡 Cần thêm param lọc `rating` (cột đã có trong DB) |
| **4. Cart** | Màn 11: Giỏ hàng | `GET /api/v1/cart`, `POST /items`, `PATCH /items/{id}`, `DELETE /items/{id}` | 🔴 **Cần thêm `sellerId`, `shopName`, `stockQuantity`, `productId` vào `CartItemDto`** |
| | Badge giỏ hàng | `GET /api/v1/cart/count` | 🟢 Tiện ích bổ sung |
| **5. Checkout** | Màn 12: Đặt hàng | `POST /api/v1/buyer/orders` | ✅ Đã có sẵn |
| | Tính thử tiền trước đặt | `POST /api/v1/buyer/orders/preview` | 🟢 Cần thêm (Tính toán In-Memory, không đổi DB) |
| | Màn 13: Chọn Voucher | `GET /api/v1/buyer/promotions`, `POST /buyer/promotions/validate` | ✅ Đã có sẵn 100% |
| | Màn 14: VNPay WebView | Nhận `paymentUrl` từ Order hoặc gọi `POST /orders/{id}/pay` | 🟡 Cần thêm endpoint `/orders/{id}/pay` khi thanh toán lại |
| | Màn 15: Kết quả đặt | Xử lý từ response tạo đơn / return URL | ✅ Đã có sẵn |
| **6. Orders** | Màn 16: Lịch sử đơn | `GET /api/v1/buyer/orders` | 🔴 **Cần thêm ảnh, tên SP đại diện, tên Shop vào `OrderSummaryDto`** |
| | Màn 17: Chi tiết & Lộ trình | `GET /api/v1/buyer/orders/{id}` | ✅ Đã có sẵn 100% (đầy đủ các mốc thời gian) |
| | Màn 18: Hủy đơn | `PATCH /api/v1/buyer/orders/{id}/cancel` | ✅ Đã có sẵn 100% |
| | Yêu cầu trả hàng | `POST /api/v1/buyer/orders/{id}/return-request` | ✅ Đã có sẵn 100% |
| | Màn 19: Viết đánh giá | `POST /api/v1/buyer/reviews` | ✅ Đã có sẵn 100% |
| **7. Profile** | Màn 20: Trang cá nhân | `GET /api/v1/users/me` | ✅ Đã có sẵn 100% |
| | Yêu thích (Wishlist) | `GET /api/v1/wishlist`, `POST /wishlist/products/{id}` | ✅ Đã có sẵn 100% (`WishlistController`) |
| | Chat với Shop | `POST /api/v1/conversations?sellerId=...`, `/messages` | ✅ Đã có sẵn 100% (`ChatController`) |
| | Màn 21: Sửa hồ sơ & Avatar| `PUT /api/v1/users/me`, `POST /api/v1/users/me/avatar` | ✅ Đã có sẵn 100% |
| | Màn 22: Đổi mật khẩu | `POST /api/v1/users/change-password` | ✅ Đã có sẵn 100% |
| | Màn 23, 24: Sổ địa chỉ | CRUD `GET/POST/PUT/DELETE /api/v1/users/addresses` | ✅ Đã có sẵn 100% |

---

## 2. P0 - NHÓM YÊU CẦU CẤP THIẾT (BLOCK UI / LỖI NGHIỆP VỤ - ZERO DB CHANGES)

### 2.1. Bổ sung thông tin Shop và Tồn kho vào `CartItemDto`
* **Vấn đề thực tế:** 
  Hàm checkout tại [`OrderServiceImpl.java:269`](file:///d:/b2c-vaa/b2c-be/src/main/java/com/example/B2C/modules/order/service/OrderServiceImpl.java#L269) quy định đơn hàng chỉ được chứa sản phẩm của 1 Shop duy nhất. Do `CartItemDto` thiếu `sellerId`, Mobile không thể gom nhóm giỏ hàng theo Shop, cũng không biết tồn kho khả dụng để giới hạn số lượng.
* **Cách ánh xạ không đổi DB:**
  - `CartItem` -> lấy `ProductVariant` -> lấy `Product` -> lấy `Seller`:
    - `productId = item.getVariant().getProduct().getId()`
    - `productSlug = item.getVariant().getProduct().getSlug()`
    - `sellerId = item.getVariant().getProduct().getSeller().getId()`
    - `shopName = item.getVariant().getProduct().getSeller().getShopName()`
    - `stockQuantity = item.getVariant().getStockQuantity()`
    - `isAvailable = item.getVariant().getIsActive() && (item.getVariant().getStockQuantity() > 0)`
* **DTO sau cập nhật:**
```java
public class CartItemDto {
    private Long id;
    private Long variantId;
    private String variantName;
    private String productName;
    private String thumbnailUrl;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal lineTotal;
    private Boolean isSelected;
    private LocalDateTime addedAt;

    // === CÁC TRƯỜNG THÊM MỚI (LẤY TỪ RELATION HIỆN TẠI) ===
    private Long productId;          // ID sản phẩm cha
    private String productSlug;      // Slug sản phẩm cha
    private Long sellerId;           // BẮT BUỘC để mobile gom nhóm giỏ theo Shop
    private String shopName;         // Tên shop bán món này
    private Integer stockQuantity;   // Tồn kho khả dụng hiện tại
    private Boolean isAvailable;     // true nếu còn hàng và variant đang active
}
```

---

### 2.2. Bổ sung tóm tắt sản phẩm đại diện vào `OrderSummaryDto`
* **Vấn đề thực tế:** 
  Màn hình Lịch sử đơn hàng hiển thị danh sách dạng Card (Tên shop, ảnh SP đầu tiên, tên SP đầu tiên, "và X sản phẩm khác"). Hiện tại `OrderSummaryDto` chỉ có thông tin tổng tiền, buộc Mobile phải gọi thêm N lần API chi tiết (N+1 queries).
* **Cách ánh xạ không đổi DB:**
  - `shopName`: Lấy từ `SellerRepository.findById(order.getSellerId()).map(Seller::getShopName).orElse(null)`.
  - Món đại diện: Trong `orderItemRepository.findByOrderId(order.getId())`, lấy phần tử đầu tiên (`items.get(0)`):
    - `firstProductName = firstItem.getProductName()`
    - `firstProductThumbnail = firstItem.getImageUrl()`
    - `firstVariantName = firstItem.getVariantName()`
    - `totalItemsCount = items.size()` (hoặc tổng quantity của các items).
* **DTO sau cập nhật:**
```java
public class OrderSummaryDto {
    private Long id;
    private String orderCode;
    private Long buyerId;
    private Long sellerId;
    private String receiverName;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    // === CÁC TRƯỜNG THÊM MỚI (KHÔNG SỬA BẢNG ORDERS) ===
    private String shopName;
    private String firstProductName;
    private String firstProductThumbnail;
    private String firstVariantName;
    private Integer totalItemsCount;
}
```

---

## 3. P1 - NHÓM CHỨC NĂNG QUAN TRỌNG (ZERO DB CHANGES)

### 3.1. API lấy chi tiết sản phẩm theo ID (`GET /api/v1/products/id/{id}`)
* **Vấn đề thực tế:** Mobile app khi mở từ Giỏ hàng, Đơn hàng, Deep Link chỉ mang theo `productId` (Long), không có `slug`.
* **Cách thực hiện không đổi DB:**
  - Thêm endpoint vào `ProductController`:
    ```java
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetailById(@PathVariable Long id) {
        ProductDetailDto product = productService.getProductDetailById(id);
        productService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    ```
  - Trong `ProductServiceImpl`, sử dụng hàm đã có: `productRepository.findByIdAndDeletedAtIsNull(id)`.

---

### 3.2. API lấy lại URL thanh toán cho đơn hàng (`POST /api/v1/buyer/orders/{id}/pay`)
* **Vấn đề thực tế:** Khi thanh toán VNPay bị huỷ/mất kết nối giữa chừng, đơn hàng lưu ở trạng thái `PENDING`. Khách hàng bấm "Thanh toán ngay" tại màn hình Chi tiết đơn hàng thì BE cần cấp lại link VNPay mới.
* **Cách thực hiện không đổi DB:**
  - Thêm endpoint vào `BuyerOrderController`:
    ```java
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Map<String, String>>> retryPayment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        String paymentUrl = orderService.retryPaymentForBuyer(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment URL generated", Map.of("paymentUrl", paymentUrl)));
    }
    ```
  - Logic: Kiểm tra `order.buyerId == userId`, kiểm tra `order.status == PENDING`, gọi `vnPayService.createPaymentUrl(order, clientIp)`. Bảng `payment` cập nhật lại record PENDING cũ.

---

### 3.3. Bổ sung `optionValueIds` vào `ProductVariantDto`
* **Vấn đề thực tế:** Để Mobile vẽ bộ nút chọn phân loại 2 chiều (Màu sắc, Kích cỡ), Mobile cần biết biến thể này tương ứng với những giá trị thuộc tính nào (`optionValueIds: [1, 4]`).
* **Cách ánh xạ không đổi DB:**
  - Bảng `variant_option_value` đã có sẵn trong database (`variant_id`, `option_value_id`).
  - Trong mapper `toDetailDto`: chỉ cần lấy danh sách `option_value_id` ứng với mỗi biến thể đưa vào `List<Long> optionValueIds`.
```java
public class ProductVariantDto {
    private Long id;
    private String sku;
    private String variantName;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;

    // === BỔ SUNG ===
    private List<Long> optionValueIds; // Ví dụ: [1, 4]
}
```

---

## 4. P2 - NHÓM TIỆN ÍCH & TỐI ƯU HÓA TRẢI NGHIỆM (ZERO DB CHANGES)

### 4.1. API tính trước đơn hàng (`POST /api/v1/buyer/orders/preview`)
* **Mục đích:** Trả về số tiền chính xác để hiển thị trước khi khách bấm "Đặt hàng" (Subtotal, Shipping Fee 30.000 VNĐ, Voucher Discount, Final Total).
* **Cách thực hiện không đổi DB:**
  - Endpoint: `POST /api/v1/buyer/orders/preview`
  - Input: `cartItemIds`, `addressId`, `voucherCode`.
  - Logic: Lấy các `CartItem` từ DB, tính `subtotal = sum(priceSnapshot * quantity)`. Nếu có voucher thì gọi logic của `PromotionService.validatePromotion(...)`. Cộng trừ ra `totalAmount`.
  - **Hoàn toàn tính toán trên bộ nhớ (In-memory), không lưu bản ghi nào xuống DB.**

---

### 4.2. API Đăng xuất (`POST /api/v1/auth/logout`) - Xử lý không cần DB
* **Vấn đề thực tế:** Hiện tại Backend dùng **Stateless JWT** và **không có bảng `refresh_token` trong DB**.
* **Nguyên tắc không đổi DB:**
  - **TUYỆT ĐỐI KHÔNG TẠO BẢNG MỚI ĐỂ LƯU TOKEN**.
  - Endpoint: `POST /api/v1/auth/logout`.
  - Nghiệp vụ: Phản hồi HTTP 200 OK để Mobile App dọn sạch token trong bộ nhớ lưu trữ an toàn (AsyncStorage / SecureStore).
  - *(Tùy chọn nâng cao nếu BE muốn chặn token)*: Lưu `jti` hoặc chuỗi token đã logout vào In-Memory Cache (Caffeine Cache hoặc ConcurrentHashMap) có TTL tự hết hạn theo thời gian sống của token, không ghi xuống Postgres.

---

### 4.3. API Xóa nhiều mục trong giỏ hàng (`DELETE /api/v1/cart/items`)
* **Cách thực hiện không đổi DB:**
  - Endpoint: `DELETE /api/v1/cart/items`
  - Request Body: `{"cartItemIds": [10, 11, 12]}`
  - Logic: `cartItemRepository.deleteAllById(request.getCartItemIds())` với điều kiện các mục thuộc giỏ của user hiện tại.

---

### 4.4. Mở rộng bộ lọc Catalog (`brand`, `minRating`)
* **Cách thực hiện không đổi DB:**
  - Bảng `product` đã có sẵn 2 cột: `brand` (varchar) và `rating_avg` (decimal).
  - Bổ sung vào `ProductSearchRequest`:
    - `private String brand;`
    - `private BigDecimal minRating;`
  - Bổ sung điều kiện `WHERE` trong câu query Specification/JPA Repository:
    `(:brand IS NULL OR p.brand = :brand) AND (:minRating IS NULL OR p.ratingAvg >= :minRating)`.

---

### 4.5. Lọc đánh giá sản phẩm theo số sao (`rating`)
* **Cách thực hiện không đổi DB:**
  - Bảng `review` đã có sẵn cột `rating` (từ 1 đến 5).
  - Cập nhật endpoint: `GET /api/v1/products/{productId}/reviews?rating=5&page=0&size=10`.
  - Thêm `@RequestParam(required = false) Integer rating` vào `ReviewController.listByProduct`.

---

### 4.6. API lấy nhanh số lượng món trong giỏ (`GET /api/v1/cart/count`)
* **Cách thực hiện không đổi DB:**
  - Phục vụ hiển thị số Badge trên icon giỏ hàng ở Header Trang chủ và Header Chi tiết sản phẩm mà không cần tải cả list sản phẩm.
  - Endpoint: `GET /api/v1/cart/count`
  - Logic: `cartItemRepository.countByCartId(cart.getId())`.
  - Response: `{"code": 200, "data": {"count": 3}}`.

---

## 5. LƯU Ý XỬ LÝ LUỒNG VNPAY TRÊN MOBILE (WEBVIEW INTERCEPT)

Do backend hiện tại redirect về URL web:
```java
response.sendRedirect(frontendBase + "/payment/return?status=success&orderCode=" + parsed.getOrderCode());
```
* **Giải pháp chuẩn trên Mobile (Không cần sửa logic redirect của BE):**
  1. Mobile mở `paymentUrl` trong WebView.
  2. Mobile cài đặt bộ chặn URL:
     - React Native: `onNavigationStateChange = (navState) => { if (navState.url.includes('/payment/return')) { ... } }`
     - Flutter: `NavigationDelegate(onNavigationRequest: (request) => { if (request.url.contains('/payment/return')) { ... } })`
  3. Khi phát hiện URL chứa `/payment/return`, Mobile trích xuất query param `status=success/failed` và `orderCode`, lập tức đóng WebView và chuyển hướng sang Màn hình Kết quả đặt hàng (Màn 15).

---

## 6. BẢNG TỔNG HỢP CHECKLIST CHO BACKEND DEVELOPER

> [!NOTE]
> Mọi công việc dưới đây chỉ can thiệp vào các file Java (DTO, Service, Controller, Repository). **Không tạo bất kỳ file migration Flyway nào.**

| Ưu tiên | Hạng mục công việc | File can thiệp | Thay đổi DB? |
| :---: | :--- | :--- | :---: |
| 🔴 **P0** | Thêm `sellerId`, `shopName`, `productId`, `productSlug`, `stockQuantity`, `isAvailable` vào `CartItemDto` | `CartItemDto.java`<br>`CartService.java` | ❌ **Không** (Lấy qua relation) |
| 🔴 **P0** | Thêm `shopName`, `firstProductName`, `firstProductThumbnail`, `firstVariantName`, `totalItemsCount` vào `OrderSummaryDto` | `OrderSummaryDto.java`<br>`OrderServiceImpl.java` | ❌ **Không** (Lấy từ item đầu) |
| 🟡 **P1** | Endpoint `GET /api/v1/products/id/{id}` | `ProductController.java`<br>`ProductService.java` | ❌ **Không** (Dùng findById) |
| 🟡 **P1** | Endpoint `POST /api/v1/buyer/orders/{id}/pay` | `BuyerOrderController.java`<br>`OrderService.java` | ❌ **Không** (Dùng VnPayService) |
| 🟡 **P1** | Thêm `optionValueIds` vào `ProductVariantDto` | `ProductVariantDto.java`<br>`ProductServiceImpl.java` | ❌ **Không** (Bảng variant_option_value đã có) |
| 🟢 **P2** | Endpoint `POST /api/v1/buyer/orders/preview` | `BuyerOrderController.java`<br>`OrderService.java` | ❌ **Không** (Tính toán In-Memory) |
| 🟢 **P2** | Endpoint `POST /api/v1/auth/logout` | `AuthController.java` | ❌ **Không** (Stateless wipe) |
| 🟢 **P2** | Endpoint `DELETE /api/v1/cart/items` (xóa nhiều) | `CartController.java`<br>`CartService.java` | ❌ **Không** (Xóa theo list ID) |
| 🟢 **P2** | Thêm param `brand`, `minRating` vào Catalog search | `ProductSearchRequest.java`<br>`ProductRepository.java` | ❌ **Không** (Cột đã có trong bảng product) |
| 🟢 **P2** | Thêm param `rating` vào Review list | `ReviewController.java`<br>`ReviewRepository.java` | ❌ **Không** (Cột đã có trong bảng review) |
| 🟢 **P2** | Endpoint `GET /api/v1/cart/count` | `CartController.java`<br>`CartService.java` | ❌ **Không** (Dùng countByCartId) |
