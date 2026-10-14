# Member 7: Admin / technical integration

Read ../00_START_HERE.md first. Your existing branch is `feature/admin-management`. Merge the reviewed migration from origin/development before coding. Your table/API key is `admins`.

## 1. Open your own files

- `backend/src/main/java/com/realestate/model/Admin.java`
- `backend/src/main/java/com/realestate/model/SuperAdmin.java`
- `backend/src/main/java/com/realestate/model/ContentAdmin.java`
- `backend/src/main/java/com/realestate/dao/AdminDAO.java`
- `frontend/src/pages/AdminsPage.jsx`

Extra ownership: Shared config/Database.java, dao/SqlDAO.java, PortalApplication, PortalController, ApiErrors, PortalService, Schema, SQL schema, pom.xml, GitHub Actions and packaging.

These files already exist in the SQL package. Edit them, rather than create duplicate classes. Java packages remain com.realestate.model / dao / service / controller / config / util. Java root is backend/src/main/java. If rebuilding a missing file, right-click its package → New Java Class; React files are under frontend/src/pages.

## 2. Understand your OOP model

Admin is abstract and extends User. Leaf classes SuperAdmin, ContentAdmin implement `getPermissions()` and summary(). Each constructor takes ID and a map of fields and calls super. Entity encapsulates an immutable snapshot. Schema constructs your subtype using the discriminator field. There are no JPA @Entity annotations because this version uses JDBC.

Read these files and explain one subtype behavior with concrete input/output. Keep calculations/business distinctions in Java, not copied into React. Do not remove inheritance just because the storage changed to SQL.

## 3. Replace file persistence with your typed SQL DAO

```java
package com.realestate.dao;
import com.realestate.model.Admin;
public final class AdminDAO extends SqlDAO<Admin> {
  public AdminDAO() { super("admins"); }
}
```

Inherited SqlDAO.findAll/findById/create/update/delete use SELECT/INSERT/UPDATE/DELETE with parameters. Read SqlDAO to explain where your CRUD SQL executes. Database.jdbc supplies the Spring-managed connection pool. Do not create a private connection per React request or put credentials in your DAO.

## 4. Review the table and service rules

Open database/01_schema.sql and find CREATE TABLE `admins`. Understand its primary key, column types and any foreign/unique/check constraints. Database fields match Schema field names. Never modify column names without updating schema, Schema, service and forms together.

Required module rules: unique account names; password hashing; SUPER/CONTENT roles; only Super manages admins; final super cannot delete/demote; audit logging; transaction rollback.

Search PortalService for `"admins"` and review save/delete plus canCreate/canRead/canEdit/canDelete. Generic/default logic may cover some cases. Coordinate shared edits with member 7. The SQL backend keeps these existing rules; do not bypass them by executing raw INSERTs from the browser.

## 5. Connect your frontend through the API

Open frontend/src/pages/AdminsPage.jsx. It delegates to ModulePage with module="admins". This creates four screen states: list, create, detail, edit. If adding module-specific usability changes, keep the common API contract. Shared component/CSS changes need member 2 review.

Read frontend/src/api.js; it handles JSON, cookies, CSRF and errors. The calls are:

```javascript
// Examples inside your React component after initialization/login:
const rows = await api('/records/admins');
const one = await api('/records/admins/' + id);
const created = await api('/records/admins', {method:'POST', body:JSON.stringify(formData)});
const edited = await api('/records/admins/' + id, {method:'PUT', body:JSON.stringify(formData)});
await api('/records/admins/' + id, {method:'DELETE'});
```

id and formData refer to actual component state, not undefined placeholders to paste into a terminal. formData contains string-valued Schema fields. Vite forwards /api to Java. PortalController checks access and delegates to PortalService. No separate per-member server or CORS wildcard is needed.

## 6. Run and test your part

1. Prepare your local MySQL using the shared setup. Java environment variables must match that database.
2. Start backend on8081 and frontend on5173 using the shared commands. Sign in as the role relevant to your module.
3. Test: Create content admin → login/read/edit → reject admin-management action → delete temporary admin; verify last-super protection, audit rows and rollback behavior.
4. Run database/member_queries/7_admins.sql in Workbench to confirm rows actually changed. Do not display password columns in evidence.
5. Restart backend and confirm SQL data persists. H2 preview resets when stopped; MySQL does not.
6. Run mvn test in backend and the frontend build. Add meaningful module regression checks through member 4 when you change business behavior.
7. Capture all four screen states and successful/failed CRUD/permission evidence.

## 7. Commit, push and review

With finished reviewed work in your branch:

```bash
git status
git add backend/src/main/java/com/realestate/model/Admin.java backend/src/main/java/com/realestate/model/SuperAdmin.java backend/src/main/java/com/realestate/model/ContentAdmin.java backend/src/main/java/com/realestate/dao/AdminDAO.java frontend/src/pages/AdminsPage.jsx
git commit -m "Implement and verify admins SQL workflow"
git push -u origin feature/admin-management
```

Stage any actual additional changed shared files explicitly after reviewing them; the add command lists your core owned files only. Do not make an empty/artificial commit if no changes were made. Open PR `feature/admin-management` → development, include real test evidence and request the relevant owners' review. Main receives the final tested integration.

## 8. Explain your completed part

Show your four CRUD screens, request-to-service-to-SQL flow, subtype method, constraints, permissions, transaction boundary, actual Git contributions and MySQL persistence evidence. Explain inherited shared code honestly. Your component reference source is provided as Member_7_Component_Source.text; build from normal files, not that reading copy.
