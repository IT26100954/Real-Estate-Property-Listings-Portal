-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT * FROM `reviews`;
SELECT COUNT(*) AS record_count FROM `reviews`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT * FROM `reviews` WHERE id = 'ACTUAL_ID';
