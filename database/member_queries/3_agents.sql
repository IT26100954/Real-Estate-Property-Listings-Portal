-- Run in MySQL Workbench after your CRUD actions through the UI.
USE real_estate_portal;
SELECT * FROM `agents`;
SELECT COUNT(*) AS record_count FROM `agents`;
-- Replace ACTUAL_ID with a saved ID to verify a single record.
SELECT * FROM `agents` WHERE id = 'ACTUAL_ID';
