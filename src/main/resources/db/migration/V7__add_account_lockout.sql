-- Account lockout support: track consecutive failed logins and a temporary lock window.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS lock_until TIMESTAMP NULL;
