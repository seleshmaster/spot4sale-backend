-- V15__create_host_tables.sql
-- Incremental Flyway migration for Spot4Sale DB
-- Adds Host Type and Host Category

-- 1) Host Type
CREATE TABLE IF NOT EXISTS host_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL
    );

-- 2) Host Category
CREATE TABLE IF NOT EXISTS host_category (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    host_type_id UUID NOT NULL REFERENCES host_type(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL
    );

-- 3) Optional: add indexes for performance
CREATE INDEX IF NOT EXISTS idx_host_type_name ON host_type(name);
CREATE INDEX IF NOT EXISTS idx_host_category_name ON host_category(name);
CREATE INDEX IF NOT EXISTS idx_host_category_type_id ON host_category(host_type_id);
