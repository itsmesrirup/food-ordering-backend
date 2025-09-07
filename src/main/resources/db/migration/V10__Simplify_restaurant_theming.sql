-- Remove the old, complex color columns
ALTER TABLE restaurant
DROP COLUMN IF EXISTS theme_primary_color,
DROP COLUMN IF EXISTS theme_secondary_color,
DROP COLUMN IF EXISTS theme_background_color,
DROP COLUMN IF EXISTS theme_paper_color,
DROP COLUMN IF EXISTS theme_text_color_primary,
DROP COLUMN IF EXISTS theme_text_color_secondary,
DROP COLUMN IF EXISTS theme_background_image_url;

-- Add the new, simpler theme control columns
ALTER TABLE restaurant
ADD COLUMN IF NOT EXISTS use_dark_theme BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS hero_image_url VARCHAR(255);