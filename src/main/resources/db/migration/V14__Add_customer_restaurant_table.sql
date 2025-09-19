-- Create join table to link global customers to restaurants

CREATE TABLE IF NOT EXISTS customer_restaurant (
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    restaurant_id BIGINT NOT NULL REFERENCES restaurant(id) ON DELETE CASCADE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    loyalty_points INT DEFAULT 0,
    PRIMARY KEY (customer_id, restaurant_id)
);

-- Optional: Add indices for fast lookup
CREATE INDEX IF NOT EXISTS idx_customer_restaurant_customer ON customer_restaurant(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_restaurant_restaurant ON customer_restaurant(restaurant_id);