-- Requires pgcrypto for UUIDs; comment out if you already enabled it earlier.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS store_open_season (
                                                 id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    -- Optional weekly pattern: 1..7 (Mon..Sun), NULL/empty = all days
    open_weekdays int[],
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (start_date <= end_date)
    );

CREATE INDEX IF NOT EXISTS idx_store_open_season_store ON store_open_season (store_id);
CREATE INDEX IF NOT EXISTS idx_store_open_season_range ON store_open_season (store_id, start_date, end_date);
