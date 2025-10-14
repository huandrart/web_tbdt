-- Migration: Update role enum to include SHIPPER
-- This migration updates the role column to support the new SHIPPER role

-- First, let's check the current role column definition
-- The role column should be ENUM('USER','ADMIN','SUPER_ADMIN','SHIPPER')

-- Update the role column to include SHIPPER
ALTER TABLE users MODIFY COLUMN role ENUM('USER','ADMIN','SUPER_ADMIN','SHIPPER') NOT NULL DEFAULT 'USER';

-- Verify the change
-- You can run this query to check: SHOW COLUMNS FROM users LIKE 'role';
