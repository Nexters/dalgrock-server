-- Add deleted_at column for soft delete (run manually if needed)
ALTER TABLE records ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
