-- Add content column for report JSON payload (run manually if needed)
ALTER TABLE reports ADD COLUMN IF NOT EXISTS content TEXT;
