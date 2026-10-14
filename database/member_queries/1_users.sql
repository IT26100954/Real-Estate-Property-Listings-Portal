-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT id, username, email, userType FROM `users`;
SELECT COUNT(*) AS record_count FROM `users`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT id, username, email FROM `users` WHERE id = 'ACTUAL_ID';
