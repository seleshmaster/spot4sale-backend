-- Add a simple policy to each store: how many hours before the start a paid booking can be cancelled for a refund.
ALTER TABLE store
    ADD COLUMN IF NOT EXISTS cancellation_cutoff_hours INTEGER;

-- Backfill + enforce default (24 hours) for existing rows
UPDATE store SET cancellation_cutoff_hours = 24
WHERE cancellation_cutoff_hours IS NULL;

ALTER TABLE store
    ALTER COLUMN cancellation_cutoff_hours SET NOT NULL,
ALTER COLUMN cancellation_cutoff_hours SET DEFAULT 24;

-- (Optional) Booking refund/audit columns if you don’t already have them
ALTER TABLE booking
    ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS refund_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS refund_amount_cents BIGINT,
    ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255);
