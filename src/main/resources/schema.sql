-- =========================================================
-- B2C MARKETPLACE DATABASE
-- PostgreSQL
-- =========================================================

-- =========================================================
-- 1. ROLE
-- =========================================================

CREATE TABLE IF NOT EXISTS role (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(30) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO role (code, name, description)
VALUES
    ('BUYER',  'Buyer',  'Customer who purchases products'),
    ('SELLER', 'Seller', 'User who sells products'),
    ('ADMIN',  'Admin',  'System administrator')
ON CONFLICT (code) DO NOTHING;


-- =========================================================
-- 2. USER
-- =========================================================

CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL,

    email               VARCHAR(255) NOT NULL UNIQUE,
    phone               VARCHAR(20) UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,

    full_name           VARCHAR(150) NOT NULL,
    avatar_url          TEXT,

    gender              VARCHAR(20)
        CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),

    date_of_birth       DATE,

    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'LOCKED', 'BANNED')),

    email_verified_at   TIMESTAMP,
    phone_verified_at   TIMESTAMP,
    last_login_at       TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
        REFERENCES role(id)
);


-- =========================================================
-- 3. ADDRESS
-- =========================================================

CREATE TABLE IF NOT EXISTS address (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,

    recipient_name      VARCHAR(150) NOT NULL,
    phone               VARCHAR(20) NOT NULL,

    province_code       VARCHAR(20),
    district_code       VARCHAR(20),
    ward_code           VARCHAR(20),

    street_detail       VARCHAR(255) NOT NULL,
    full_address        TEXT NOT NULL,

    type                VARCHAR(20) DEFAULT 'HOME'
        CHECK (type IN ('HOME', 'OFFICE')),

    is_default          BOOLEAN NOT NULL DEFAULT FALSE,

    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_address_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);


-- =========================================================
-- 4. SELLER
-- =========================================================

CREATE TABLE IF NOT EXISTS seller (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,

    shop_name           VARCHAR(150) NOT NULL,
    slug                VARCHAR(200) NOT NULL UNIQUE,

    logo_url            TEXT,
    banner_url          TEXT,
    description         TEXT,

    business_type       VARCHAR(20) NOT NULL
        CHECK (business_type IN ('INDIVIDUAL', 'COMPANY')),

    tax_code            VARCHAR(50),
    id_card_number      VARCHAR(50),

    pickup_address_id   BIGINT,

    rating_avg          DECIMAL(3, 2) NOT NULL DEFAULT 0,
    rating_count        INTEGER NOT NULL DEFAULT 0,
    follower_count      INTEGER NOT NULL DEFAULT 0,
    total_product       INTEGER NOT NULL DEFAULT 0,

    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),

    approved_at         TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_seller_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_seller_pickup_address
        FOREIGN KEY (pickup_address_id)
        REFERENCES address(id)
);


-- =========================================================
-- 5. CATEGORY
-- =========================================================

CREATE TABLE IF NOT EXISTS category (
    id                  BIGSERIAL PRIMARY KEY,

    parent_id           BIGINT,

    name                VARCHAR(150) NOT NULL,
    slug                VARCHAR(200) NOT NULL UNIQUE,
    icon_url            TEXT,

    level               INTEGER NOT NULL DEFAULT 1,
    path                VARCHAR(500),

    sort_order          INTEGER NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
        REFERENCES category(id)
);


-- =========================================================
-- 6. PRODUCT
-- =========================================================

CREATE TABLE IF NOT EXISTS product (
    id                  BIGSERIAL PRIMARY KEY,

    seller_id           BIGINT NOT NULL,
    category_id         BIGINT NOT NULL,

    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(300) NOT NULL UNIQUE,

    description         TEXT,
    brand               VARCHAR(150),

    thumbnail_url       TEXT,

    -- Denormalized price for displaying product listing
    min_price           DECIMAL(15, 2) NOT NULL DEFAULT 0,
    max_price           DECIMAL(15, 2) NOT NULL DEFAULT 0,

    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (
            status IN (
                'DRAFT',
                'PENDING',
                'ACTIVE',
                'HIDDEN',
                'BANNED'
            )
        ),

    rating_avg          DECIMAL(3, 2) NOT NULL DEFAULT 0,
    rating_count        INTEGER NOT NULL DEFAULT 0,
    sold_count          INTEGER NOT NULL DEFAULT 0,
    view_count          BIGINT NOT NULL DEFAULT 0,

    weight_gram         INTEGER,
    length_cm           DECIMAL(10, 2),
    width_cm            DECIMAL(10, 2),
    height_cm           DECIMAL(10, 2),

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT fk_product_seller
        FOREIGN KEY (seller_id)
        REFERENCES seller(id),

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES category(id)
);


-- =========================================================
-- 7. PRODUCT VARIANT
-- =========================================================

CREATE TABLE IF NOT EXISTS product_variant (
    id                  BIGSERIAL PRIMARY KEY,

    product_id          BIGINT NOT NULL,

    sku                 VARCHAR(100) NOT NULL UNIQUE,
    variant_name        VARCHAR(255) NOT NULL,

    price               DECIMAL(15, 2) NOT NULL,
    sale_price          DECIMAL(15, 2),

    stock_quantity      INTEGER NOT NULL DEFAULT 0,
    reserved_quantity   INTEGER NOT NULL DEFAULT 0,

    sold_count          INTEGER NOT NULL DEFAULT 0,

    image_url           TEXT,
    weight_gram         INTEGER,

    barcode             VARCHAR(100),

    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_variant_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT chk_variant_price
        CHECK (price >= 0),

    CONSTRAINT chk_variant_stock
        CHECK (
            stock_quantity >= 0
            AND reserved_quantity >= 0
            AND reserved_quantity <= stock_quantity
        )
);


-- =========================================================
-- 8. PRODUCT OPTION
-- =========================================================

CREATE TABLE IF NOT EXISTS product_option (
    id                  BIGSERIAL PRIMARY KEY,

    product_id          BIGINT NOT NULL,

    name                VARCHAR(100) NOT NULL,
    sort_order          INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_option_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT uq_product_option
        UNIQUE (product_id, name)
);


-- =========================================================
-- 9. PRODUCT OPTION VALUE
-- =========================================================

CREATE TABLE IF NOT EXISTS product_option_value (
    id                  BIGSERIAL PRIMARY KEY,

    option_id           BIGINT NOT NULL,

    value               VARCHAR(100) NOT NULL,
    image_url           TEXT,

    sort_order          INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_option_value_option
        FOREIGN KEY (option_id)
        REFERENCES product_option(id),

    CONSTRAINT uq_option_value
        UNIQUE (option_id, value)
);


-- =========================================================
-- 10. PRODUCT IMAGE
-- =========================================================

CREATE TABLE IF NOT EXISTS product_image (
    id                  BIGSERIAL PRIMARY KEY,

    product_id          BIGINT NOT NULL,

    image_url           TEXT NOT NULL,
    alt_text            VARCHAR(255),

    sort_order          INTEGER NOT NULL DEFAULT 0,
    is_thumbnail        BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_product_image_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
);


-- =========================================================
-- 11. VARIANT OPTION VALUE
-- =========================================================

CREATE TABLE IF NOT EXISTS variant_option_value (
    variant_id          BIGINT NOT NULL,
    option_value_id     BIGINT NOT NULL,

    PRIMARY KEY (variant_id, option_value_id),

    CONSTRAINT fk_variant_option_variant
        FOREIGN KEY (variant_id)
        REFERENCES product_variant(id),

    CONSTRAINT fk_variant_option_value
        FOREIGN KEY (option_value_id)
        REFERENCES product_option_value(id)
);


-- =========================================================
-- 12. CART
-- =========================================================

CREATE TABLE IF NOT EXISTS cart (
    id                  BIGSERIAL PRIMARY KEY,

    user_id             BIGINT NOT NULL UNIQUE,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);


-- =========================================================
-- 13. CART ITEM
-- =========================================================

CREATE TABLE IF NOT EXISTS cart_item (
    id                  BIGSERIAL PRIMARY KEY,

    cart_id             BIGINT NOT NULL,
    variant_id          BIGINT NOT NULL,

    quantity            INTEGER NOT NULL DEFAULT 1,

    price_snapshot      DECIMAL(15, 2) NOT NULL,

    is_selected         BOOLEAN NOT NULL DEFAULT TRUE,

    added_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id)
        REFERENCES cart(id),

    CONSTRAINT fk_cart_item_variant
        FOREIGN KEY (variant_id)
        REFERENCES product_variant(id),

    CONSTRAINT uq_cart_variant
        UNIQUE (cart_id, variant_id),

    CONSTRAINT chk_cart_quantity
        CHECK (quantity > 0)
);


-- =========================================================
-- 14. ORDER
-- =========================================================

CREATE TABLE IF NOT EXISTS orders (
    id                  BIGSERIAL PRIMARY KEY,

    order_code          VARCHAR(50) NOT NULL UNIQUE,

    buyer_id            BIGINT NOT NULL,
    seller_id           BIGINT NOT NULL,

    -- Snapshot receiver information
    receiver_name       VARCHAR(150) NOT NULL,
    receiver_phone      VARCHAR(20) NOT NULL,
    shipping_address    TEXT NOT NULL,

    subtotal            DECIMAL(15, 2) NOT NULL DEFAULT 0,
    shipping_fee        DECIMAL(15, 2) NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(15, 2) NOT NULL DEFAULT 0,
    platform_discount   DECIMAL(15, 2) NOT NULL DEFAULT 0,

    total_amount        DECIMAL(15, 2) NOT NULL DEFAULT 0,

    payment_method      VARCHAR(20) NOT NULL
        CHECK (
            payment_method IN (
                'COD',
                'VNPAY',
                'MOMO',
                'CARD'
            )
        ),

    payment_status      VARCHAR(20) NOT NULL DEFAULT 'UNPAID'
        CHECK (
            payment_status IN (
                'UNPAID',
                'PAID',
                'REFUNDED'
            )
        ),

    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING',
                'CONFIRMED',
                'PACKED',
                'SHIPPING',
                'DELIVERED',
                'COMPLETED',
                'CANCELLED',
                'RETURNED'
            )
        ),

    note                TEXT,
    cancel_reason       TEXT,
    cancelled_by        BIGINT,

    confirmed_at        TIMESTAMP,
    shipped_at          TIMESTAMP,
    delivered_at        TIMESTAMP,
    completed_at        TIMESTAMP,
    cancelled_at        TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_buyer
        FOREIGN KEY (buyer_id)
        REFERENCES users(id),

    CONSTRAINT fk_order_seller
        FOREIGN KEY (seller_id)
        REFERENCES seller(id),

    CONSTRAINT fk_order_cancelled_by
        FOREIGN KEY (cancelled_by)
        REFERENCES users(id)
);


-- =========================================================
-- 15. ORDER ITEM
-- =========================================================

CREATE TABLE IF NOT EXISTS order_item (
    id                  BIGSERIAL PRIMARY KEY,

    order_id            BIGINT NOT NULL,

    variant_id          BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,

    -- Snapshot product information
    product_name        VARCHAR(255) NOT NULL,
    variant_name        VARCHAR(255),

    image_url           TEXT,

    unit_price          DECIMAL(15, 2) NOT NULL,
    quantity            INTEGER NOT NULL,

    discount            DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_price         DECIMAL(15, 2) NOT NULL,

    is_reviewed         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_order_item_variant
        FOREIGN KEY (variant_id)
        REFERENCES product_variant(id),

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT chk_order_item_quantity
        CHECK (quantity > 0)
);


-- =========================================================
-- 16. PAYMENT
-- =========================================================

CREATE TABLE IF NOT EXISTS payment (
    id                  BIGSERIAL PRIMARY KEY,

    order_id            BIGINT NOT NULL,

    method              VARCHAR(20) NOT NULL,
    provider            VARCHAR(50),

    transaction_id      VARCHAR(255),

    amount              DECIMAL(15, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'VND',

    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING',
                'SUCCESS',
                'FAILED',
                'REFUNDED'
            )
        ),

    paid_at             TIMESTAMP,
    refunded_at         TIMESTAMP,
    refund_amount       DECIMAL(15, 2),

    gateway_response    JSONB,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);


-- =========================================================
-- 17. SHIPMENT
-- =========================================================

CREATE TABLE IF NOT EXISTS shipment (
    id                      BIGSERIAL PRIMARY KEY,

    order_id                BIGINT NOT NULL UNIQUE,

    carrier_code            VARCHAR(30),
    tracking_number         VARCHAR(100),

    service_type            VARCHAR(20)
        CHECK (service_type IN ('STANDARD', 'EXPRESS')),

    shipping_fee            DECIMAL(15, 2) NOT NULL DEFAULT 0,
    cod_amount              DECIMAL(15, 2) NOT NULL DEFAULT 0,

    from_address            TEXT,
    to_address              TEXT,

    status                  VARCHAR(20) NOT NULL DEFAULT 'PICKING'
        CHECK (
            status IN (
                'PICKING',
                'PICKED',
                'TRANSIT',
                'DELIVERING',
                'DELIVERED',
                'FAILED',
                'RETURNED'
            )
        ),

    estimated_delivery_date DATE,

    picked_at               TIMESTAMP,
    delivered_at            TIMESTAMP,

    note                    TEXT,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipment_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);


-- =========================================================
-- 18. PROMOTION
-- =========================================================

CREATE TABLE IF NOT EXISTS promotion (
    id                  BIGSERIAL PRIMARY KEY,

    seller_id           BIGINT,

    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(150) NOT NULL,

    discount_type       VARCHAR(20) NOT NULL
        CHECK (
            discount_type IN (
                'PERCENT',
                'FIXED',
                'FREESHIP'
            )
        ),

    discount_value      DECIMAL(15, 2) NOT NULL,
    max_discount_amount DECIMAL(15, 2),

    min_order_value     DECIMAL(15, 2) DEFAULT 0,

    quantity            INTEGER NOT NULL,
    used_count          INTEGER NOT NULL DEFAULT 0,

    limit_per_user      INTEGER DEFAULT 1,

    apply_scope         VARCHAR(20) NOT NULL DEFAULT 'ALL'
        CHECK (
            apply_scope IN (
                'ALL',
                'CATEGORY',
                'PRODUCT'
            )
        ),

    start_at            TIMESTAMP NOT NULL,
    end_at              TIMESTAMP NOT NULL,

    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (
            status IN (
                'DRAFT',
                'ACTIVE',
                'EXPIRED',
                'DISABLED'
            )
        ),

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_promotion_seller
        FOREIGN KEY (seller_id)
        REFERENCES seller(id),

    CONSTRAINT chk_promotion_date
        CHECK (end_at > start_at)
);


-- =========================================================
-- 19. REVIEW
-- =========================================================

CREATE TABLE IF NOT EXISTS review (
    id                  BIGSERIAL PRIMARY KEY,

    order_item_id       BIGINT NOT NULL UNIQUE,

    product_id          BIGINT NOT NULL,
    variant_id          BIGINT,
    user_id             BIGINT NOT NULL,
    seller_id           BIGINT NOT NULL,

    rating              INTEGER NOT NULL
        CHECK (rating BETWEEN 1 AND 5),

    comment             TEXT,

    image_urls          JSONB,
    video_url           TEXT,

    is_anonymous        BOOLEAN NOT NULL DEFAULT FALSE,

    seller_reply        TEXT,
    replied_at          TIMESTAMP,

    like_count          INTEGER NOT NULL DEFAULT 0,

    status              VARCHAR(20) NOT NULL DEFAULT 'VISIBLE'
        CHECK (
            status IN (
                'VISIBLE',
                'HIDDEN'
            )
        ),

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_order_item
        FOREIGN KEY (order_item_id)
        REFERENCES order_item(id),

    CONSTRAINT fk_review_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT fk_review_variant
        FOREIGN KEY (variant_id)
        REFERENCES product_variant(id),

    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_review_seller
        FOREIGN KEY (seller_id)
        REFERENCES seller(id)
);


-- =========================================================
-- 20. CONVERSATION
-- =========================================================

CREATE TABLE IF NOT EXISTS conversation (
    id                  BIGSERIAL PRIMARY KEY,

    buyer_id            BIGINT NOT NULL,
    seller_id           BIGINT NOT NULL,

    last_message        TEXT,
    last_message_at     TIMESTAMP,

    buyer_unread_count  INTEGER NOT NULL DEFAULT 0,
    seller_unread_count INTEGER NOT NULL DEFAULT 0,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_buyer
        FOREIGN KEY (buyer_id)
        REFERENCES users(id),

    CONSTRAINT fk_conversation_seller
        FOREIGN KEY (seller_id)
        REFERENCES seller(id),

    CONSTRAINT uq_buyer_seller
        UNIQUE (buyer_id, seller_id)
);


-- =========================================================
-- 21. MESSAGE
-- =========================================================

CREATE TABLE IF NOT EXISTS message (
    id                  BIGSERIAL PRIMARY KEY,

    conversation_id     BIGINT NOT NULL,
    sender_id           BIGINT NOT NULL,

    sender_type         VARCHAR(20) NOT NULL
        CHECK (
            sender_type IN (
                'BUYER',
                'SELLER'
            )
        ),

    content             TEXT,

    message_type        VARCHAR(20) NOT NULL DEFAULT 'TEXT'
        CHECK (
            message_type IN (
                'TEXT',
                'IMAGE',
                'PRODUCT',
                'ORDER'
            )
        ),

    attachment_url      TEXT,

    ref_product_id      BIGINT,
    ref_order_id        BIGINT,

    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversation(id),

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id),

    CONSTRAINT fk_message_product
        FOREIGN KEY (ref_product_id)
        REFERENCES product(id),

    CONSTRAINT fk_message_order
        FOREIGN KEY (ref_order_id)
        REFERENCES orders(id)
);


-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX IF NOT EXISTS idx_address_user ON address(user_id);
CREATE INDEX IF NOT EXISTS idx_seller_status ON seller(status);
CREATE INDEX IF NOT EXISTS idx_category_parent ON category(parent_id);
CREATE INDEX IF NOT EXISTS idx_product_seller ON product(seller_id);
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);
CREATE INDEX IF NOT EXISTS idx_variant_product ON product_variant(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart ON cart_item(cart_id);
CREATE INDEX IF NOT EXISTS idx_order_buyer ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_seller ON orders(seller_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_order ON payment(order_id);
CREATE INDEX IF NOT EXISTS idx_shipment_tracking ON shipment(tracking_number);
CREATE INDEX IF NOT EXISTS idx_promotion_seller ON promotion(seller_id);
CREATE INDEX IF NOT EXISTS idx_review_product ON review(product_id);
CREATE INDEX IF NOT EXISTS idx_review_user ON review(user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_buyer ON conversation(buyer_id);
CREATE INDEX IF NOT EXISTS idx_conversation_seller ON conversation(seller_id);
CREATE INDEX IF NOT EXISTS idx_message_conversation ON message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_created_at ON message(created_at);
