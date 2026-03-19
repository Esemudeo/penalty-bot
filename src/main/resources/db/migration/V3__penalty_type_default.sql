ALTER TABLE penalty_type
    ADD COLUMN is_default boolean NOT NULL DEFAULT false;

UPDATE penalty_type
SET    is_default = true
WHERE  technical_name = 'teamkill';
