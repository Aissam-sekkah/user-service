-- 1. Clean up existing roles to avoid duplicate name constraints and remove UUIDs
-- We use TRUNCATE to clear the table and reset the identity
TRUNCATE TABLE roles CASCADE;

-- 2. Seed roles using the NAME as the ID (Natural Key pattern)
-- This ensures that roleUseCase.getRoleById("ROLE_USER") actually finds the record.
INSERT INTO roles (id, name, description) VALUES 
('ROLE_USER', 'ROLE_USER', 'Standard system user with basic access'),
('ROLE_MANAGER', 'ROLE_MANAGER', 'User manager with ability to edit profiles'),
('ROLE_ADMIN', 'ROLE_ADMIN', 'System administrator with full access');
