-- V3__update_host_table.sql
-- Add missing columns to host table in spot4sale based on spot4sale_v2.host

ALTER TABLE host
-- Pricing & amenities
    ADD COLUMN default_price numeric(10,2),
ADD COLUMN default_amenities text,
-- Booking & operations
ADD COLUMN max_booths integer,
ADD COLUMN operating_hours text,
ADD COLUMN contact_email varchar(255),
ADD COLUMN contact_phone varchar(50),
ADD COLUMN tags text,
ADD COLUMN foot_traffic_estimate integer,
ADD COLUMN cancellation_policy text,
ADD COLUMN booking_window_days integer,
ADD COLUMN active boolean DEFAULT true;

-- Optional: if you want to initialize default values for existing rows
UPDATE host
SET active = true
WHERE active IS NULL;
