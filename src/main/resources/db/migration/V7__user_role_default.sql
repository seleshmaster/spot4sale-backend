-- Add role column if missing
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
     WHERE table_name = 'users' AND column_name = 'role'
  ) THEN
ALTER TABLE users ADD COLUMN role VARCHAR(32);
END IF;
END$$;

-- Normalize null/empty roles to 'USER'
UPDATE users SET role = 'USER' WHERE role IS NULL OR trim(role) = '';

-- Enforce not null + default
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
