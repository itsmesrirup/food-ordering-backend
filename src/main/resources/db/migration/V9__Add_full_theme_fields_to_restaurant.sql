ALTER TABLE restaurant
ADD COLUMN IF NOT EXISTS theme_background_color VARCHAR(255),
ADD COLUMN IF NOT EXISTS theme_paper_color VARCHAR(255),
ADD COLUMN IF NOT EXISTS theme_text_color_primary VARCHAR(255),
ADD COLUMN IF NOT EXISTS theme_text_color_secondary VARCHAR(255),
ADD COLUMN IF NOT EXISTS theme_background_image_url VARCHAR(255);