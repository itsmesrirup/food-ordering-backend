-- Add guest order support to orders table
-- V15__Add_guest_order_support.sql

-- Add guest fields to orders table for orders placed without customer accounts
ALTER TABLE orders ADD COLUMN guest_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN guest_email VARCHAR(255);

-- No need to modify customer_id constraint as it's already nullable in V1