
INSERT INTO users (id, name, email, role) VALUES
  (uuid_generate_v4(), 'Alice Owner', 'alice.owner@example.com', 'STORE_OWNER'),
  (uuid_generate_v4(), 'Bob Buyer', 'bob.buyer@example.com', 'USER')
ON CONFLICT DO NOTHING;

WITH owner AS ( SELECT id FROM users WHERE role='STORE_OWNER' LIMIT 1 )
INSERT INTO store (id, owner_id, name, description, address, city, zip_code, latitude, longitude)
SELECT uuid_generate_v4(), owner.id, 'Mission Market', 'Great Saturday spots', '123 Valencia St', 'San Francisco', '94110', 37.7599, -122.4148 FROM owner
ON CONFLICT DO NOTHING;

WITH owner AS ( SELECT id FROM users WHERE role='STORE_OWNER' LIMIT 1 )
INSERT INTO store (id, owner_id, name, description, address, city, zip_code, latitude, longitude)
SELECT uuid_generate_v4(), owner.id, 'Sunset Swap', 'Spacious lot', '456 Noriega St', 'San Francisco', '94122', 37.7544, -122.4850 FROM owner
ON CONFLICT DO NOTHING;

-- Two spots per store
INSERT INTO spot (id, store_id, price_per_day, available)
SELECT uuid_generate_v4(), s.id, 25.00, TRUE FROM store s LIMIT 2 ON CONFLICT DO NOTHING;
INSERT INTO spot (id, store_id, price_per_day, available)
SELECT uuid_generate_v4(), s.id, 40.00, TRUE FROM store s LIMIT 2 ON CONFLICT DO NOTHING;

-- Assign a demo Stripe Connected Account to the first store owner for testing
WITH owner AS (SELECT id FROM users WHERE role='STORE_OWNER' LIMIT 1)
UPDATE users u
  SET stripe_account_id = 'acct_demo_connect',
      charges_enabled = TRUE
  FROM owner
  WHERE u.id = owner.id
    AND (u.stripe_account_id IS NULL OR u.stripe_account_id = '');
