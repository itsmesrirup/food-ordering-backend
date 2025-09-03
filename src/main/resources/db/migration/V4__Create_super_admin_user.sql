-- Create a super admin user for yourself.
-- IMPORTANT: Use a secure, randomly generated password here.
-- You will use this password to log in for the first time.
-- The password hash here is for the password "superadminpassword"
-- You SHOULD generate your own hash for a more secure password.
INSERT INTO _user (id, email, password, role) VALUES
(1, 'itsmesrirup@gmail.com', '$2a$12$d/ZGh/CRhXSTqu.dYmpN5OkpCYNWg.qaZ7cfFoVRH.C24ugO2Vpb.', 'SUPER_ADMIN');

-- Update the user sequence so the next auto-generated ID doesn't conflict
-- This command syntax can vary slightly between PostgreSQL versions
-- Use this if your sequence is named _user_seq
ALTER SEQUENCE _user_seq RESTART WITH 2;
-- If your ID generation is IDENTITY, this might be more complex, but for now, this works.
-- A simpler but less clean way is just to set a high ID number like 100.