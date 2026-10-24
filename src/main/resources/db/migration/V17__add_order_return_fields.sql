-- Add return/refund fields to the orders table
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS return_reason VARCHAR(50),
    ADD COLUMN IF NOT EXISTS return_description TEXT,
    ADD COLUMN IF NOT EXISTS return_requested_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS return_approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS return_rejected_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS return_admin_note TEXT;
