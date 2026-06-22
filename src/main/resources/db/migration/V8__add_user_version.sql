-- Optimistic locking for users. Existing rows must carry a non-null version
-- (NOT NULL DEFAULT 0) so Hibernate treats them as persistent (merge), not new.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
