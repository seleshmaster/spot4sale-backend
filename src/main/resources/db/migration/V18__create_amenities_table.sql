-- V3__create_amenities_table.sql

-- Create the amenities table
CREATE TABLE IF NOT EXISTS amenities (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );

-- Create a join table to establish many-to-many relation between host and amenities
CREATE TABLE IF NOT EXISTS host_amenities (
                                              host_id UUID NOT NULL,
                                              amenity_id UUID NOT NULL,
                                              PRIMARY KEY (host_id, amenity_id),
    CONSTRAINT fk_host
    FOREIGN KEY(host_id) REFERENCES host(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_amenity
    FOREIGN KEY(amenity_id) REFERENCES amenities(id)
    ON DELETE CASCADE
    );

-- Trigger to automatically update updated_at on amenities table
CREATE OR REPLACE FUNCTION update_amenities_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_amenities_updated_at
    BEFORE UPDATE ON amenities
    FOR EACH ROW
    EXECUTE FUNCTION update_amenities_updated_at();
