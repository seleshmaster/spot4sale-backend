-- V20250901__update_store_to_host.sql
-- 1) Rename table store to host
ALTER TABLE store RENAME TO host;

-- 2) Add columns for host_type and host_category
ALTER TABLE host
    ADD COLUMN host_type_id UUID REFERENCES host_type(id) ON DELETE SET NULL,
ADD COLUMN host_category_id UUID REFERENCES host_category(id) ON DELETE SET NULL;

-- 3) Optional: Add indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_host_type_id ON host(host_type_id);
CREATE INDEX IF NOT EXISTS idx_host_category_id ON host(host_category_id);
