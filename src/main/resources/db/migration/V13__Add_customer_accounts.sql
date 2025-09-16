-- Add new columns to the customer table
ALTER TABLE customer
ADD COLUMN IF NOT EXISTS password VARCHAR(255),
ADD COLUMN IF NOT EXISTS phone VARCHAR(255),
ADD COLUMN IF NOT EXISTS birthday DATE,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS role VARCHAR(255);

-- Make the password column not nullable for new entries (after this script runs)
ALTER TABLE customer ALTER COLUMN password SET NOT NULL;

-- Make the email unique and not nullable
ALTER TABLE customer ADD CONSTRAINT uk_customer_email UNIQUE (email);
ALTER TABLE customer ALTER COLUMN email SET NOT NULL;

-- Set a default role for any existing customers
UPDATE customer SET role = 'USER' WHERE role IS NULL;