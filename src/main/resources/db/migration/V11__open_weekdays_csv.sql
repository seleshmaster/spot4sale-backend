-- src/main/resources/db/migration/V7__open_weekdays_csv.sql
ALTER TABLE store_open_season ADD COLUMN IF NOT EXISTS open_weekdays VARCHAR(64);
-- Optional: if you had an int[] column called open_weekdays, rename it first:
-- ALTER TABLE store_open_season RENAME COLUMN open_weekdays TO open_weekdays_arr;
-- (Then you could translate array -> csv with a one-off script if needed.)
