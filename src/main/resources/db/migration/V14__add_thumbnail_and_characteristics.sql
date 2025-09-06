-- V2__add_thumbnail_and_characteristics.sql

ALTER TABLE store
    -- Thumbnail: single image stored as URL or path
    ADD COLUMN thumbnail TEXT,

  -- Characteristics: JSON for flexible key-value store
  ADD COLUMN characteristics JSONB;
