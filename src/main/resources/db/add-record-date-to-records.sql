-- Add record_date column for optional record date (run manually if needed)
-- When null, record date is derived from created_at.
ALTER TABLE records ADD COLUMN IF NOT EXISTS record_date DATE;
