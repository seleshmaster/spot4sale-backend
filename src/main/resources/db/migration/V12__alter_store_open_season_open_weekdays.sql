-- VXX__alter_store_open_season_open_weekdays.sql
ALTER TABLE store_open_season
ALTER COLUMN open_weekdays TYPE VARCHAR(64)
    USING open_weekdays::VARCHAR;
