-- Add denormalised columns on cart_item so that cart/ordering modules never
-- need to traverse the ProductVariant relationship just to read variant details.
-- Values are kept in sync via JPA @PrePersist / @PreUpdate callbacks.
ALTER TABLE cart_item ADD COLUMN variant_id_denorm    BIGINT NULL;
ALTER TABLE cart_item ADD COLUMN variant_name_denorm  VARCHAR(500) NULL;
ALTER TABLE cart_item ADD COLUMN product_name_denorm  VARCHAR(500) NULL;
ALTER TABLE cart_item ADD COLUMN image_url_denorm     TEXT NULL;

-- Backfill existing rows from the existing FK join
UPDATE cart_item SET
    variant_id_denorm   = cart_item.variant_id,
    variant_name_denorm = pv.variant_name,
    product_name_denorm = p.name,
    image_url_denorm    = pv.image_url
FROM product_variant pv
JOIN product p ON p.id = pv.product_id
WHERE cart_item.variant_id = pv.id;
