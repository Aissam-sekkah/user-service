-- Migration to add refresh_token for session management
ALTER TABLE users ADD COLUMN refresh_token VARCHAR(512);
