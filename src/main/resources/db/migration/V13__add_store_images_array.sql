-- Add a column for store images as an array
ALTER TABLE store
    ADD COLUMN images TEXT[];
