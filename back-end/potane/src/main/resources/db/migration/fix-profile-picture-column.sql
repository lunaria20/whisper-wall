-- Fix profile_picture column to support large base64-encoded images
-- This script converts the profile_picture column from VARCHAR(255) to TEXT

ALTER TABLE users 
  ALTER COLUMN profile_picture TYPE TEXT;

-- If you need to apply this manually in Supabase SQL Editor, run the above command.
-- The column can now store large base64-encoded profile pictures.
