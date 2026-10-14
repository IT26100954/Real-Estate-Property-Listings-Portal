# Haven — SQL / Spring Boot / React portal

Start with **docs/00_START_HERE.md**. Follow your **docs/members/Member_N.md** next.

backend/ is Java 21 + Spring Boot 4.1.1 + Spring JDBC. frontend/ is React + Vite. database/ contains MySQL setup, schema, synthetic demo data and member verification queries. scripts/ contains optional text-data import and frontend packaging.

Development: Java on 8081, React on 5173, MySQL on 3306. Git workflow: feature branches → development → main. No new GitHub account/repository is required.

This package has complete source; no runtime text-file persistence remains. The old project's domain models/business rules have been retained. See docs/VALIDATION.md for actual verification and limits. Database credentials are environment variables and must never be committed.
