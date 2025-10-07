-- Migration script to update users table for new role system

-- Update role column to support enum values
ALTER TABLE users 
MODIFY COLUMN role ENUM('USER', 'ADMIN', 'SUPER_ADMIN') NOT NULL DEFAULT 'USER';

-- Update existing data
UPDATE users SET role = 'USER' WHERE role = 'user';
UPDATE users SET role = 'ADMIN' WHERE role = 'admin';

-- Add comment
ALTER TABLE users 
MODIFY COLUMN role ENUM('USER', 'ADMIN', 'SUPER_ADMIN') NOT NULL DEFAULT 'USER' 
COMMENT 'User role: USER, ADMIN, SUPER_ADMIN';
