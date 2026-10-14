-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT * FROM `offers`;
SELECT COUNT(*) AS record_count FROM `offers`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT * FROM `offers` WHERE id = 'ACTUAL_ID';
