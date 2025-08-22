
ALTER TABLE store
  ADD COLUMN IF NOT EXISTS location geography(Point, 4326);

UPDATE store
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE location IS NULL
  AND latitude IS NOT NULL
  AND longitude IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_store_location_gist
  ON store USING GIST (location);
