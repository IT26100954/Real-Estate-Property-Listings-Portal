-- Run as a MySQL administrator. Replace the sample password BEFORE executing.
CREATE DATABASE IF NOT EXISTS real_estate_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'portal_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_LOCAL_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON real_estate_portal.* TO 'portal_app'@'localhost';
-- Use administrator access for schema creation, not the application account.
