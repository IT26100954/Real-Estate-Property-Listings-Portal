# Architecture

```mermaid
flowchart LR
 R[React pages] --> A[api.js / Vite proxy]
 A --> C[Spring REST controller]
 C --> S[PortalService: roles and rules]
 S --> D[Typed DAOs / SqlDAO]
 D --> J[JdbcTemplate: prepared statements]
 J --> M[(MySQL)]
 C --> T[TransactionTemplate]
```

```mermaid
erDiagram
 users ||--o{ properties : owns
 agents ||--o{ properties : assigned
 properties ||--o{ appointments : viewing
 users ||--o{ appointments : buyer
 agents ||--o{ appointments : assigned
 properties ||--o{ offers : receives
 users ||--o{ offers : buyer
 users ||--o{ reviews : writes
```

Review targets refer to either properties or agents and are service-validated. Activity actor IDs may belong to users/admins; they are retained as audit identifiers without a foreign key. Java abstract model inheritance remains in model/; Schema maps table discriminators to leaves. Transactions cover mutation plus linked-email updates and audit writes. A static shared JDBC bridge preserves existing DAO constructors; it is intended for one application context/process. Later refactoring can inject DAOs into the service without changing the API.
