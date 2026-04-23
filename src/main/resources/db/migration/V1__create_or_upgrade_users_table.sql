CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

UPDATE users
SET password_hash = 'RESET_REQUIRED'
WHERE password_hash IS NULL
   OR TRIM(password_hash) = '';

ALTER TABLE users
    ALTER COLUMN password_hash SET NOT NULL;
