-- Ensure btree_gist is present for exclusion constraint support
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Optional: normalize status column contents to upper-case constants if needed

-- Prevent overlapping bookings on the same spot (except when cancelled)
-- Using an inclusive daterange: [startDate, endDate]
ALTER TABLE booking
    ADD CONSTRAINT booking_no_overlap
    EXCLUDE USING gist (
    "spot_id" WITH =,
    daterange("start_date","end_date",'[]') WITH &&
  )
  WHERE (status <> 'CANCELLED');
