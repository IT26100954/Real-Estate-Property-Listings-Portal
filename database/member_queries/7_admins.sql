-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT id, username, email, adminLevel FROM `admins`;
SELECT COUNT(*) AS record_count FROM `admins`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT id, username, email FROM `admins` WHERE id = 'ACTUAL_ID';
