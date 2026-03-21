-- Soft-delete support: inactive types are hidden from the bot but kept for historical penalty records.
ALTER TABLE penalty_type ADD COLUMN active boolean NOT NULL DEFAULT true;
UPDATE penalty_type SET technical_name = gen_random_uuid()::text;
