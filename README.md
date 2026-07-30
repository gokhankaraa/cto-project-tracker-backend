# Weekly Project Status Reporting and CTO Tracking System — Backend

Backend API where project managers enter weekly status reports for the projects they own, and the
CTO monitors the entire project portfolio from a single place.

Track: **Backend (Java + Spring Boot)** · Author: Gökhan Kara

> **Status:** T06 (days 10–12) complete — WorkItem (report line items) CRUD is working with a
> derived live-task count, on top of Project/WeeklyReport CRUD (T05) and the T04 skeleton (health
> endpoint, shared error format, Swagger). CTO dashboard is next (T07).

Related document: [`docs/on_analiz.md`](docs/on_analiz.md)

---

## Tech stack

| Layer | Choice | Rationale |
|---|---|---|
| Language | Java 21 (LTS) | LTS release; compatibility with the libraries used is verified |
| Framework | Spring Boot 4.1 | Start a new project on a current version; natural support for layered architecture |
| Data access | Spring Data JPA (Hibernate) | Entity–table mapping, repository abstraction |
| Database | H2 (embedded, in-memory) | Zero setup; portable to PostgreSQL thanks to the repository abstraction |
| Validation | Spring Validation (Bean Validation) | Field-level required/format/range checks |
| API docs | springdoc-openapi 3.0.3 (Swagger UI) | Test endpoints from the browser and show the API in the demo |
| Helper | Lombok | Reduce boilerplate getter/setter/constructor code |

> springdoc's 2.x line targets Spring Boot 3; with Boot 4 the **3.x** line must be used.

## Requirements

- JDK 21 or newer (`java -version`)
- No need to install Maven — the bundled Maven Wrapper (`./mvnw`) is used
- No separate database setup needed; H2 runs in memory alongside the application

## Setup and run

```bash
git clone <repository-url>
cd cto-project-tracker-backend

./mvnw spring-boot:run          # starts the application (http://localhost:8080)
```

Alternatively, build and run a jar:

```bash
./mvnw clean package
java -jar target/cto-project-tracker-backend-0.0.1-SNAPSHOT.jar
```

### Verifying it runs

| What | Address | Expected |
|---|---|---|
| Health endpoint | http://localhost:8080/api/health | `status: UP`, `databaseStatus: UP` |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation opens |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | Returns the OpenAPI schema |
| H2 console | http://localhost:8080/h2-console | JDBC URL: `jdbc:h2:mem:ctotracker`, user: `sa`, empty password |

```bash
curl http://localhost:8080/api/health
```

```json
{
  "status": "UP",
  "application": "cto-project-tracker-backend",
  "databaseStatus": "UP",
  "database": "H2 2.4.240 (2025-09-22)",
  "timestamp": "2026-07-22T15:49:49.528096+03:00"
}
```

The `databaseStatus` field is produced by actually issuing a query against the database; it confirms
not just that the application is up, but that the DB connection works.

## Configuration

All settings live in [`src/main/resources/application.yml`](src/main/resources/application.yml).
The development environment contains no secret values (H2 user `sa`, empty password). When moving to
PostgreSQL, connection details will be supplied via environment variables and never committed.

| Setting | Default | Description |
|---|---|---|
| `server.port` | `8080` | Application port |
| `spring.datasource.url` | `jdbc:h2:mem:ctotracker` | In-memory H2; data is dropped when the app stops |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Schema is generated from entities on each startup |
| `spring.h2.console.enabled` | `true` | H2 web console (development only) |

## API endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | Application and DB health check |
| GET / POST | `/api/projects` | List / create projects |
| GET / PUT / DELETE | `/api/projects/{id}` | Project detail / update / delete |
| GET / POST | `/api/projects/{projectId}/reports` | List / create weekly reports for a project |
| GET / PUT / DELETE | `/api/reports/{id}` | Weekly report detail / update / delete |
| GET / POST | `/api/reports/{reportId}/work-items` | List / create work items for a report |
| GET / PUT / DELETE | `/api/work-items/{id}` | Work item detail / update / delete |

The full, interactive contract is in Swagger UI. Key business rules (see pre-analysis §H-03/H-04):

- A project can have only **one report per week** — a duplicate `(project, week)` returns **409**.
- Progress is tracked as ordered stages `ANALIZ → GELISTIRME → TEST → TAMAMLANDI`; the percentage
  (`25 / 50 / 75 / 100`) is **derived from the stage, never entered by hand**.
- On update a report's stage may stay the same or advance exactly one step; going backward or
  skipping a stage returns **400**.
- Work items belong to a report; deleting a report also deletes its work items (cascade). The
  report's **live task count** is derived from the number of `DEVAM_EDIYOR` work items — not stored
  separately.
- A project that still has weekly reports cannot be deleted (**409**); delete its reports first.

## Demo data

On startup, `DataSeeder` loads sample data into the in-memory H2 (only when the DB is empty), so
Swagger and the endpoints are usable immediately without manual setup:

- **Users:** 2 project managers, 1 CTO, 1 admin (e.g. `ayse@kolaysoft.com` — PM, id 1)
- **Projects:** PEYK, e-Donusum, EczaciPOS (assigned to the PMs)
- **Weekly reports:** a few reports across the projects

## Package and layer structure

```
com.kolaysoft.ctotracker
├── config/           Application configuration (OpenAPI/Swagger)
├── controller/       HTTP layer: request handling, DTO exchange
├── service/          Business rules (stage transitions, uniqueness)
├── repository/       Database access via Spring Data JPA
├── entity/           JPA data model and enums
├── dto/              Request/response contracts (entities are not exposed directly)
└── common/
    ├── error/        ApiError, ErrorCode, GlobalExceptionHandler
    └── exception/    ResourceNotFoundException, DuplicateResourceException, BusinessRuleException
```

Layering rule: `controller → service → repository`. Controllers contain no business rules, services
don't know about HTTP, and entities are not exposed directly to the API (DTOs are used).

## Error format

Whatever the error, the API returns a single body shape (see pre-analysis, section 11):

```json
{
  "timestamp": "2026-07-22T15:49:49.598473+03:00",
  "status": 404,
  "code": "RESOURCE_NOT_FOUND",
  "message": "Proje bulunamadi: 42",
  "path": "/api/projects/42"
}
```

Validation errors additionally include a `fieldErrors` array:

```json
{
  "timestamp": "2026-07-22T15:49:49.598473+03:00",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Gonderilen alanlar dogrulama kurallarina uymuyor.",
  "path": "/api/reports",
  "fieldErrors": [
    { "field": "weekNumber", "message": "Hafta numarasi en fazla 53 olmalidir." }
  ]
}
```

| Code | HTTP | When |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Missing required field, format/range error |
| `INVALID_REQUEST` | 400 | Malformed JSON, unknown enum value, bad date |
| `BUSINESS_RULE_VIOLATION` | 400 | Business-rule violation (e.g. skipping a progress stage) |
| `RESOURCE_NOT_FOUND` | 404 | Record or endpoint not found |
| `METHOD_NOT_ALLOWED` | 405 | Endpoint does not support this HTTP method |
| `DUPLICATE_RESOURCE` | 409 | Uniqueness violation (project+week, email) |
| `RESOURCE_IN_USE` | 409 | Resource can't be deleted because other records depend on it (e.g. a project that still has reports) |
| `INTERNAL_ERROR` | 500 | Unexpected error; technical detail is logged, not leaked to the client |

## Tests

```bash
./mvnw test
```

Current tests (32 in total):

- `HealthControllerTest` — health endpoint and DB connection
- `GlobalExceptionHandlerTest` — the error-format contract (400 / 404 / 405 / 409 / 500)
- `WeeklyReportServiceTest` — business rules: uniqueness (409), stage skip/backward (400),
  percentage derivation, not-found (404)
- `ProjectServiceTest` — project create/mapping, owner-not-found (404), delete-with-reports (409)
- `WorkItemServiceTest` — work item CRUD, report-not-found (404), delete
- `CtoProjectTrackerBackendApplicationTests` — does the Spring context load

## Known gaps

MVP scope decisions, with rationale, are in sections 13–14 of the pre-analysis document.
Not yet implemented at this stage:

- CTO dashboard summary endpoint (T07)
- Authentication (login) and role-based authorization — out of MVP scope; `User.password` is
  omitted for now and will arrive with authentication
- Dashboard filters, report locking, audit log, export — out of MVP scope
