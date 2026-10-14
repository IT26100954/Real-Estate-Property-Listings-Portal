-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT * FROM `properties`;
SELECT COUNT(*) AS record_count FROM `properties`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT * FROM `properties` WHERE id = 'ACTUAL_ID';
