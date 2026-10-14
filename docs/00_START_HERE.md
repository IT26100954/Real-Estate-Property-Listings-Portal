# SQL + Spring Boot + React migration: start here

This is the SQL replacement for the supplied seven-module JSP/Servlet/text-file project. It is a separate source package. Your actual GitHub screenshots show a small Spring Boot starter, not all of the earlier portal source; this package supplies the domain models, SQL DAOs, service rules, REST API and React screens together. Keep your existing Git history and review the migration in a branch.

## Stack and why

Use Java 21 + Spring Boot 4.1.1 for the backend, MySQL for data, and React + Vite for the frontend. Node.js runs frontend development/build tools; it is not a second backend. Spring JDBC/JdbcTemplate provides prepared SQL and connection pooling. JPA/Hibernate is not needed here: direct SQL preserves your existing inheritance and makes CRUD easy to explain in a SQL/OOP viva.

This is more setup than keeping JSP and changing only storage. Since you specifically requested React and Spring Boot, this package connects them while reusing your current business rules. Spring Boot includes its own embedded server: do not copy this JAR into old Tomcat 10.1 webapps. Old servlet/JSP run instructions do not apply to the migrated version.

```text
React form / browser on localhost:5173
 → fetch('/api/...') → Vite proxy → Spring Boot on localhost:8081
 → PortalController → PortalService → typed DAO → SqlDAO/JdbcTemplate
 → MySQL on localhost:3306 → JSON response → updated React screen
```

For a final local build, React assets can be packaged inside the Spring Boot JAR, so the browser uses one origin on localhost:8081. GitHub stores source and collaboration history; GitHub Pages cannot run this Java backend or MySQL database.

## 1. Prerequisites — every member

1. Keep your existing GitHub account/collaborator setup and existing branches. You do not need a new repository.
2. Install JDK 21; check `java -version` and `javac -version` in a new terminal. IntelliJ Project SDK and Maven runner JDK must be 21.
3. Install Node.js LTS, version 22.12+ or a newer supported LTS. Check `node -v` and `npm -v`. Node includes npm.
4. Install MySQL Server (MySQL 8.4 LTS is a suitable choice) and MySQL Workbench using the official MySQL downloads. Workbench is the client; Server must also be installed and running.
5. Keep IntelliJ's working Git executable/account setup. Use a short clone path such as `C:\Projects\real-estate-portal` to avoid your earlier Windows filename-length problem.
6. Use IntelliJ's bundled Maven, or install Maven for terminal commands. `mvn` not recognized does not mean Java is broken.

## 2. Leader introduces the migration once

1. Back up the old source and existing text data outside the repository. Do not expose personal runtime data on GitHub.
2. In your existing clone, commit finished work or stash unfinished changes before switching branches. Run `git status` and confirm you understand the working tree.
3. Fetch and switch to your existing development branch:

```bash
git fetch origin
git switch development
git pull --ff-only
git switch -c feature/sql-migration
```

If development exists only remotely, use `git switch -c development --track origin/development` instead of the second line. If sql-migration already exists, switch to it without `-c`.
4. Extract this ZIP into a temporary folder. Copy its `backend`, `frontend`, `database`, `scripts`, `docs` and `.github` folders into the root of the existing clone. Preserve the clone's `.git` directory. Merge `.gitignore` entries into your current file rather than accidentally replacing meaningful rules.
5. Use this package's README for the new run process. Your old starter's root pom/src may remain as a reference temporarily, but all new commands must run in **backend** or **frontend** as specified. Do not mix two application classes in the new backend. You can archive obsolete starter files in a later reviewed cleanup; do not delete teammates' work blindly.
6. Run setup/build/tests below. Stage the new folders, README and intended ignore changes only. Review `git diff --cached` and make sure no passwords/private SQL exports/generated directories are staged.
7. Commit and push:

```bash
git add backend frontend database scripts docs .github README.md .gitignore
git commit -m "Introduce SQL Spring Boot backend and React frontend"
git push -u origin feature/sql-migration
```

8. Open a PR into **development**, reviewed with member 7 and member 2. Explain the stack change and your actual tests. After merge, all seven members synchronize their existing feature branches before editing.

## 3. Create MySQL database — Workbench, not Git Bash

1. Open MySQL Workbench and connect to your local server as an administrator, commonly root. This is your local MySQL password, not your GitHub password.
2. Open `database/00_create_database.sql` in Workbench. Replace `REPLACE_WITH_YOUR_LOCAL_PASSWORD` with a local development password before running. Keep that edited password out of Git; do not save the credential in the tracked SQL template. You can copy the script into an unsaved Workbench query tab and edit there.
3. Execute the statements. They create real_estate_portal and a limited portal_app account. If that account already exists, CREATE USER IF NOT EXISTS will not change its password: use the password already set, or change it deliberately through MySQL administration.
4. Select the real_estate_portal schema in Workbench. Open `database/01_schema.sql`. Add/select `USE real_estate_portal;` in your query tab, then execute the full schema as the administrator.
5. For a fresh demonstration database, execute `database/02_demo_data.sql` once in that schema. Do not run it repeatedly or mix it with an import of existing records.
6. Check:

```sql
USE real_estate_portal;
SHOW TABLES;
SELECT id, username, email, userType FROM users;
SELECT id, title, price FROM properties;
```

You should see users, admins, agents, properties, appointments, offers, reviews and activity_log. Demo appointments/offers initially contain no records. Five synthetic login accounts are provided:

| Role | Username | Password |
|---|---|---|
| Super admin | admin_main | Admin@123 |
| Content admin | content_mod | Moderator@123 |
| Buyer | kamal_buyer | Buyer@123 |
| Seller | nimali_owner | Seller@123 |
| Agent | sunil_agent | Agent@123 |

Passwords in SQL are precomputed PBKDF2 hashes compatible with the Java code. New users should be created through the app so their passwords are hashed correctly. Do not use MySQL's PASSWORD function or insert plaintext into password columns.

Each member can use a separate local database with the same schema/seeds. Sharing one remotely reachable database is not required and can make tests interfere with teammates.

## 4. Optional: import OLD text records instead of demo seeds

Skip this section if you want a clean demo database. The new application does not read/write text files at runtime.

1. Stop the old Tomcat application and back up its realestate-data folder.
2. Create a fresh empty SQL schema using steps 3.1–3.4; do NOT run demo data.
3. From the new project root run, substituting your actual old data directory:

```powershell
python .\scripts\import_text_data.py --source "C:\Users\YOUR-USER\realestate-data" --output .\database\imported_private_data.sql
```

4. This creates a SQL script; it does not connect to or mutate MySQL. It validates record counts and existing hash format, preserves IDs/hashes and decodes old escaping.
5. Open the generated script in Workbench, select the empty schema, execute, and verify counts. It imports users/admins/agents before dependent tables.
6. Any invalid reference, oversized field or numeric constraint must be repaired in the staged migration data before retrying into an empty schema. If execution errors, stop and ROLLBACK before committing; Workbench errors must not be ignored.
7. Imported data scripts contain private records and hashes and are ignored by Git. Do not upload them. Back up SQL separately using Workbench Data Export.

## 5. Open and run the Java backend

1. IntelliJ → Open → `backend/pom.xml` → load Maven project.
2. Set JDK 21; reload Maven. The MySQL Connector/J dependency is already declared. Do not manually download a driver JAR or add javax.servlet imports.
3. Create/run the main class `com.realestate.PortalApplication`. In Run → Edit Configurations, add environment variables DB_URL, DB_USER and DB_PASSWORD. Set DB_URL to `jdbc:mysql://localhost:3306/real_estate_portal`, DB_USER to `portal_app`, and DB_PASSWORD to the password you chose. Keep this run configuration private/unshared.
4. Or open a **PowerShell** terminal at the project root, then run:

```powershell
cd backend
$env:DB_URL='jdbc:mysql://localhost:3306/real_estate_portal'
$env:DB_USER='portal_app'
$env:DB_PASSWORD='YOUR_LOCAL_DATABASE_PASSWORD'
mvn spring-boot:run
```

These `$env:` commands are PowerShell syntax; do not paste them into Git Bash. If using Git Bash, set environment variables in IntelliJ instead, or use `export DB_PASSWORD='your-password'` and corresponding exports. No password belongs in tracked application.properties.
5. If mvn is unavailable, run PortalApplication through IntelliJ or use Maven tool window goals. Wait for startup on port 8081.
6. Open http://localhost:8081/api/auth/me . Expected JSON includes csrf and actor:null. This endpoint also confirms a database connection; startup alone may not open the lazy connection.

## 6. Run the React frontend

Open a SECOND terminal at the project root; keep the backend running:

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173/ . Sign in with admin_main / Admin@123 and check each module. The backend runs on 8081, frontend on 5173 and MySQL on 3306. None of these is a public URL.

The package was built here using pnpm and contains pnpm-lock.yaml for a reproducible pnpm build. For that exact dependency lock use `corepack pnpm install --frozen-lockfile` if Corepack is installed, or install pnpm according to its official docs. If your whole team chooses npm instead, one lead generates and commits package-lock.json with npm install; everyone then uses npm ci. Agree on one package manager rather than committing competing lockfiles.

## 7. How frontend/backend are connected

`frontend/src/api.js` is the only shared HTTP helper. It calls relative `/api` URLs, sends cookies, reads JSON and supplies X-CSRF-Token on mutations. `initialize()` first fetches `/api/auth/me` to obtain a session/token. Login rotates the session and returns a new token. React never receives password hashes or database credentials.

`frontend/vite.config.js` proxies /api requests to http://localhost:8081 during development. This avoids a separate cross-origin configuration. Do not call MySQL from React; browsers communicate with the Java API only.

| Action | HTTP endpoint | Backend behavior |
|---|---|---|
| List/search | GET /api/records/users?q=... | Permission-filtered records |
| Read one | GET /api/records/users/ID | Lookup and read permission |
| Create | POST /api/records/users | Validate, hash if applicable, INSERT |
| Update | PUT /api/records/users/ID | Ownership/validation, UPDATE |
| Delete | DELETE /api/records/users/ID | Permission/references, DELETE |
| Form schema | GET /api/schema/users | Field names/types, creation permission |
| Linked-record choices | GET /api/choices/users | Safe ID/label choices |
| Login/logout | POST /api/auth/login or /logout | Session and CSRF management |

Substitute the exact module keys users/properties/agents/appointments/offers/reviews/admins. Do not paste literal ID. React pages use list/create/detail/edit states inside each module rather than four JSP files. Each state is a distinct CRUD screen; if your lecturer insists on separate URL-addressable pages, add React routing as a reviewed UI task.

Example inside React after initialize/login:

```javascript
const users = await api('/records/users');
const created = await api('/records/users', {
  method: 'POST',
  body: JSON.stringify({username:'new_buyer', password:'BuyerPass@123',
    email:'new-buyer@example.test', phone:'0771234567', userType:'BUYER'})
});
```

This example is already supported by the supplied code. Use the UI to test; pasting JavaScript into Git Bash will not run a React form.

## 8. SQL and OOP code responsibilities

Models remain Java abstract classes/subclasses with immutable encapsulated fields. Schema selects the proper subtype. Typed UserDAO/PropertyDAO/etc extend SqlDAO instead of FileDAO. SqlDAO uses allowlisted table/column names and ? placeholders, so form values are bound through JDBC prepared statements.

```java
Database.jdbc().update("DELETE FROM `users` WHERE id = ?", id);
```

Do not concatenate user input into SQL. Table identifiers are only accepted through Schema.module's fixed keys. The application's service, not this raw example, checks delete permission/dependents.

PortalService retains role checks, reference rules, date/time conflicts, state transitions, password hashing and subtype calculations. PortalController converts HTTP/JSON to service calls and redacts passwords. TransactionTemplate wraps each request's SQL work so related record updates and audit insertion roll back together on failure. The shared lock coordinates validation/mutations in one Java process. This is still a single-instance coursework design: cross-table username uniqueness, polymorphic review targets and appointment conflict rules depend on the service, not complete database-level concurrent constraints across multiple app instances.

MySQL provides primary/foreign keys, uniqueness for module usernames and agent email/license, numeric/date fields and selected CHECK constraints. Review targetId can refer to either agents or properties; its type/target relationship is service-validated rather than a single SQL foreign key.

## 9. Tests — every member

From backend run `mvn test` or Maven tool window → test. Unlike the old project, these tests are in standard src/test/java and run through Maven. They use a disposable H2 SQL database in MySQL compatibility mode; your MySQL records are not reset by these tests.

Also perform manual CRUD against your own MySQL instance, compare rows in Workbench, restart Java and prove persistence, then check role/invalid-input cases from your member guide. H2 tests do not replace real MySQL testing.

## 10. Team branch workflow — existing branches

After the migration is merged into development, each member runs:

```bash
git fetch origin
git switch feature/user-management
git merge origin/development
```

Replace user-management with your existing assigned branch. If only remote, first use `git switch -c feature/user-management --track origin/feature/user-management`. Commit/stash unfinished changes before merging. Resolve conflicts with the relevant owner; do not force-push.

Work → test → review actual changed files → commit → push. Open PRs with base **development**, compare **your feature branch**. No member merges directly into main during development. Leader/member 7 review integrated development; after checks pass, PR development → main. Keep genuine commit authorship and acknowledge reference/generated code under course rules.

## 11. Build one final local webpage application

Stop the development servers before using the same ports. In frontend:

```powershell
npm run build
```

At project ROOT:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\package-frontend.ps1
```

In backend:

```powershell
mvn clean package
java -jar target/real-estate-portal.jar
```

Set DB_URL/DB_USER/DB_PASSWORD in that Java terminal or its run configuration first. The package script copies generated React assets into backend static resources; rebuild Java AFTER this step. Open http://localhost:8081/ . The JAR now serves frontend and API together, with MySQL still running separately. No external Tomcat installation or Node process is needed for this final local build.

Do not upload target JARs/node_modules/database secrets as source commits. Generated files can be attached to a release if your lecturer wants a compiled artifact. GitHub Actions included here builds/tests the backend and frontend; it does not deploy a public site. Public hosting later requires a Java-capable host and MySQL service, HTTPS, environment secrets and backups. Never try to put DB_PASSWORD in a VITE_ environment variable; those values can be exposed in browser assets.

## 12. Common errors

| Error | Fix |
|---|---|
| Access denied for portal_app | Match the MySQL account/password; credentials are unrelated to GitHub. |
| Unknown database/table | Run database scripts against the selected schema; runtime auto-schema creation is disabled. |
| Communications link failure | Start MySQL Server and check host/port, not only Workbench. |
| npm/mvn not recognized | Install Node/Maven or use IntelliJ Maven; reopen terminal after PATH changes. |
| Frontend says backend unavailable | Start Java on 8081; verify /api/auth/me; inspect Java console. |
| 403 | Check login role or reload to obtain current CSRF token. |
| Duplicate/FK validation | Use unique fields and real linked IDs; delete child records before parents. |
| Port already in use | Stop your old process or consistently change backend port and Vite proxy. |
| Wrong app appears | New frontend is 5173; final new app is 8081. Old 8080/real-estate-portal is JSP app. |

Official references: Spring Boot https://docs.spring.io/spring-boot/system-requirements.html ; JDBC https://docs.spring.io/spring-framework/reference/data-access/jdbc.html ; MySQL driver https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html ; React https://react.dev/learn ; Vite https://vite.dev/guide/ .
