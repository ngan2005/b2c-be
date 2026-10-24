# Câu 2 – Các giải pháp xử lý đồng thời

Dự án B2C Marketplace xử lý đồng thời bằng **bốn cơ chế bổ trợ cho nhau**:
optimistic locking, retry có giới hạn, idempotency dựa trên DB record, và domain
event với transactional listener. Mỗi cơ chế đều có code chạy thật trong repo
(`B2C/src/main/java/com/example/B2C/...`), không phải cấu hình lý thuyết.

---

## 1. Optimistic Locking (Khóa lạc quan)

Sử dụng trường `@Version` trong JPA để phát hiện khi nhiều giao dịch cùng cập
nhật một sản phẩm. Khi một giao dịch cập nhật tồn kho, hệ thống kiểm tra
phiên bản dữ liệu có thay đổi hay không.

- Nếu phiên bản không thay đổi, cập nhật thành công.
- Nếu phiên bản đã thay đổi, giao dịch bị từ chối (`ObjectOptimisticLockingFailureException`)
  để tránh ghi đè dữ liệu của giao dịch khác.

**Triển khai trong dự án:**

- `ProductVariant` có trường `@Version`; Hibernate tự tăng version và so sánh khi `saveAndFlush`.
- `E1OptimisticStrategy` (`modules/inventory/strategy/E1OptimisticStrategy.java`)
  dùng cho luồng reserve tồn kho: đọc variant → giảm `stockQuantity` → tăng
  `reservedQuantity` → `repository.saveAndFlush(v)`. Nếu hai request cùng đọc
  version `N`, request sau sẽ fail khi commit do version đã là `N+1`.
- Bên cạnh đó hệ thống còn `E2PessimisticStrategy` (dùng `SELECT ... FOR UPDATE`
  với `REQUIRES_NEW` + `READ_COMMITTED`) để so sánh với E1 khi cần mức cô lập
  mạnh hơn.

**Ví dụ:** Hai người cùng mua sản phẩm cuối cùng, chỉ một giao dịch cập nhật
tồn kho thành công; giao dịch còn lại nhận exception và được xử lý bởi cơ chế
retry hoặc trả về thông báo hết hàng.

---

## 2. Retry (Thử lại)

Khi giao dịch thất bại do xung đột đồng thời, hệ thống cho phép thử lại một số
lần giới hạn.

- Đọc lại tồn kho mới nhất.
- Kiểm tra số lượng hàng còn lại.
- Thực hiện lại giao dịch nếu điều kiện mua hàng vẫn hợp lệ.
- Nếu hết hàng hoặc vượt quá số lần thử, trả về thông báo phù hợp
  (`OutOfStockException` hoặc ném lại exception gốc).

Nên sử dụng retry có giới hạn, kết hợp thời gian chờ tăng dần (exponential
backoff) để tránh gây quá tải hệ thống. **Hiện tại dự án đang dùng linear
backoff** (`backoffMs * attempt`) – vẫn hiệu quả nhưng khi mở rộng quy mô nên
chuyển sang `backoffMs * 2^attempt` kèm jitter.

**Triển khai trong dự án:**

- `E3OptimisticRetryStrategy` (`modules/inventory/strategy/E3OptimisticRetryStrategy.java`)
  bọc E1 trong một vòng lặp bounded retry.
- Cấu hình qua `application.yml`:
  - `inventory.locking.retry.max-attempts` (mặc định `3`)
  - `inventory.locking.retry.backoff-ms` (mặc định `50`)
- Mỗi lần retry đọc lại variant từ DB, kiểm tra `stockQuantity >= qty`, sau đó
  `saveAndFlush` lại. Nếu lại dính `ObjectOptimisticLockingFailureException` thì
  tiếp tục retry cho đến khi hết `max-attempts` thì throw exception gốc.
- Có test chuyên biệt `InventoryStrategyConcurrencyTest` mô phỏng 20 request
  đồng thời trên `stock=10` để chứng minh không oversell.

**Ví dụ:** Người dùng A và B cùng bấm mua variant cuối cùng, request của A
thành công ở attempt 1, request của B fail ở attempt 1 do version conflict → retry
ở attempt 2, lúc này đã đọc version mới và phát hiện `stockQuantity < qty` →
trả về `OutOfStockException`.

---

## 3. Idempotency (Tính bất biến khi lặp lại yêu cầu)

Sử dụng `Idempotency-Key` duy nhất cho mỗi yêu cầu quan trọng (đặt hàng, thanh
toán) để tránh tạo nhiều đơn hàng khi người dùng nhấn nút mua nhiều lần hoặc hệ
thống gửi lại request do timeout.

- Lưu khóa định danh cùng **kết quả xử lý** (response payload) của yêu cầu
  vào bảng `idempotency_record` với 3 trạng thái: `IN_PROGRESS`, `DONE`, `FAILED`.
- Request body được **hash bằng SHA-256** để phát hiện cùng key nhưng body khác.
- Nếu nhận lại yêu cầu có cùng key + cùng hash + trạng thái `DONE`, hệ thống
  trả về kết quả đã lưu (replay) thay vì chạy lại nghiệp vụ.
- Nếu cùng key nhưng hash khác → ném `IdempotencyConflictException` (lạm dụng key).
- Nếu `IN_PROGRESS` → ném `IdempotencyConflictException` (request trùng đang chạy).
- Bảo đảm việc kiểm tra khóa và xử lý đặt hàng được thực hiện an toàn khi có
  nhiều request đồng thời thông qua:
  - `@Transactional(propagation = REQUIRES_NEW)` trên `IdempotencyService.executeOrReplay`
    → record commit độc lập với transaction nghiệp vụ.
  - Unique constraint trên `(scope, idempotency_key)` + bắt
    `DataIntegrityViolationException` → chống hai request cùng key chèn song
    song vào DB.

**Triển khai trong dự án:**

- `IdempotencyService` (`common/idempotency/IdempotencyService.java`) – service
  dùng chung với hai overload: replay ra `Object.class` (legacy) và replay ra
  DTO cụ thể (mới).
- Controller expose header `Idempotency-Key`:
  - `BuyerOrderController.create` – `@RequestHeader(value = "Idempotency-Key", required = false)`
  - `PaymentController.create` – cùng pattern.
- `OrderServiceImpl.createOrderForBuyer` gọi `idempotencyService.executeOrReplay(...)`
  bọc toàn bộ luồng tạo order.
- Có test tương ứng: `IdempotencyServiceTest`, `IdempotencyTypeReplayTest`,
  `OrderCreationTest#idempotency-key replays the same DTO without duplicating work`.

**Ví dụ:** Người dùng nhấn thanh toán hai lần (hoặc FE retry do network timeout) –
cả hai request mang cùng `Idempotency-Key` → request đầu chạy nghiệp vụ và ghi
record `DONE`; request thứ hai phát hiện record `DONE` với hash trùng → replay
lại response cũ, không tạo đơn hàng thứ hai.

---

## 4. Domain Event (Sự kiện nghiệp vụ)

Sau khi nghiệp vụ lõi thành công (đặt hàng, thanh toán, vận chuyển, hủy đơn),
hệ thống phát sinh các sự kiện nghiệp vụ như `OrderCreated`, `OrderPaid`,
`OrderShipped`, `OrderCancelled`, `SellerApplicationCreated`,
`CommissionRuleApplied` thông qua `ApplicationEventPublisher`.

- Các thành phần nhận sự kiện (ví dụ `SseOrderEventBridge`, `OrderEventBroadcaster`,
  `AdminEventBroadcaster`, `SellerEventBroadcaster`, `SellerNotificationService`)
  xử lý tiếp như gửi thông báo realtime tới admin dashboard, cập nhật trạng
  thái hiển thị, đồng bộ dữ liệu dashboard.
- Tất cả listener quan trọng đều dùng
  `@TransactionalEventListener(phase = AFTER_COMMIT)` → đảm bảo **chỉ phát sóng
  khi transaction database đã commit thành công**. Nếu transaction rollback thì
  sự kiện không được gửi đi, tránh thông báo "ảo" cho client.
- Cơ chế này chạy **in-process** tới `SseEmitterRegistry` (real-time SSE), nên
  chưa cần message broker.
- Khi mở rộng sang message broker (Kafka, RabbitMQ), nên kết hợp
  **Transactional Outbox** để đảm bảo cặp "DB commit ↔ event publish" không lệch
  nhau (chính DB lưu event, một worker riêng đọc DB và publish lên broker), và
  consumer cần xử lý **idempotent** (dựa trên `eventId` + DB unique key) để
  tránh xử lý trùng khi broker redeliver.

**Triển khai trong dự án:**

- `DomainEvent` (`common/event/DomainEvent.java`) – base class mở rộng
  `org.springframework.context.ApplicationEvent`.
- Các event cụ thể: `OrderCreatedEvent`, `OrderPaidEvent`, `OrderShippedEvent`,
  `OrderCancelledEvent`, `SellerApplicationCreatedEvent`,
  `CommissionRuleAppliedEvent`, `DisputeOpenedEvent`,
  `AdminDashboardSnapshotEvent`, `ReturnRequestedEvent`.
- Publisher: `OrderServiceImpl`, `PaymentService`, `SellerApplicationServiceImpl`,
  `SellerNotificationService` – gọi `eventPublisher.publishEvent(...)` ngay sau
  khi nghiệp vụ DB thành công.
- Listener `AFTER_COMMIT`: `SseOrderEventBridge`, `OrderEventBroadcaster`,
  `AdminEventBroadcaster`, `SellerEventBroadcaster` – broadcast SSE tới admin
  browser đang subscribe `/api/v1/admin/sse/orders`, `/api/v1/admin/sse/returns`.

**Ví dụ:** Khi đơn hàng được tạo thành công, `OrderServiceImpl` gọi
`eventPublisher.publishEvent(new OrderCreatedEvent(...))`. Transaction commit →
`SseOrderEventBridge.onOrderCreated` chạy ngay sau đó và push event
`ORDER_CREATED` tới tất cả admin browser đang mở trang Orders → danh sách đơn
cập nhật realtime mà không cần reload.
