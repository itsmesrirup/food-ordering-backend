-- This script alters the internal sequence that PostgreSQL creates
-- for the restaurant table's IDENTITY column.

-- The sequence name is typically in the format: <table_name>_<column_name>_seq
-- In our case, it will be 'restaurant_id_seq'.

-- Alter the sequence to set a new starting point AND a new increment.
ALTER SEQUENCE restaurant_id_seq
    RESTART WITH 1234567
    INCREMENT BY 789;