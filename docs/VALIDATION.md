# Verification and limits

Verified on 13 October 2026:

- Java 21 / Spring Boot 4.1.1: `mvn clean package` passed and generated a runnable JAR containing the React static build.
- Three HTTP + SQL integration tests passed (zero failures/errors). The CRUD test iterates all seven modules, verifies SQL row creation/update/deletion, hidden password fields and audit records. Additional tests verify CSRF, forbidden roles, invalid values, reference protection and rollback of a record write if audit insertion fails.
- Tests used isolated H2 in MySQL compatibility mode. No user MySQL database was mutated. Live MySQL deployment has not been verified here; run the supplied setup and manual verification queries against each member's local MySQL server before submission.
- React production build passed with the supplied pnpm lockfile; resolved React/React DOM 19.3.0 and Vite 8.3.4.
- Browser preview against the isolated SQL backend confirmed login, role-dependent navigation, user list and create form. A narrow viewport also verified the responsive navigation/layout. A full automated browser CRUD pass was not run; HTTP integration tests cover all backend CRUD.
- Text-data importer accepted the original synthetic sample files and produced a private staging SQL script. It validates field counts/hash format; it does not automatically fix invalid references or execute MySQL.

Runtime storage is SQL, not text files. The old FileDAO/FileUtil/servlets/JSP runtime are absent from this new package. Java models and PortalService rules have been reused. Review targets and cross-table usernames are service-validated, and a single-JVM lock coordinates conflict checks; multiple independent backend instances are outside this coursework scope. The MySQL schema has selected unique/foreign/check constraints, and mutations plus audit/email synchronization execute in a JDBC transaction.

Development uses cookies and CSRF on the same origin via Vite proxy. This local coursework package does not include email verification/password reset, rate limiting, actual payment/video services, listing image uploads or an Internet hosting deployment. Configure HTTPS/secure cookies, private secrets and backups if a public deployment is later requested.

The temporary browser preview uses test-only in-memory SQL. It is stopped after verification. Your persistent project should run with MySQL using the documented environment variables.
