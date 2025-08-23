-- Store-level blackout dates (no bookings allowed on these days)
CREATE TABLE IF NOT EXISTS store_blackout (
                                              id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    day         DATE NOT NULL,
    reason      TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (store_id, day)
    );

-- OPTIONAL (if you want weekly schedule now; otherwise skip and add later)
-- Indicates which weekdays the store is open. If table is empty, treat as "open all days except blackouts".
CREATE TABLE IF NOT EXISTS store_weekly_open (
                                                 id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1=Mon .. 7=Sun (ISO)
    open        BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (store_id, day_of_week)
    );
