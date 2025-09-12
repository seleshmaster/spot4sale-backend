-- V20250831_02__rename_tables.sql
-- Rename tables from store/spot naming to host/booth naming

-- Rename spot -> booth
ALTER TABLE IF EXISTS spot RENAME TO booth;

-- Rename store_blackout -> host_blackout
ALTER TABLE IF EXISTS store_blackout RENAME TO host_blackout;

-- Rename store_open_season -> host_open_season
ALTER TABLE IF EXISTS store_open_season RENAME TO host_open_season;

-- Rename store_weekly_open -> host_weekly_open
ALTER TABLE IF EXISTS store_weekly_open RENAME TO host_weekly_open;
