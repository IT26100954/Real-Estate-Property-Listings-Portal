-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT * FROM `appointments`;
SELECT COUNT(*) AS record_count FROM `appointments`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT * FROM `appointments` WHERE id = 'ACTUAL_ID';
