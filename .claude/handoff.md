# HOEM — Handoff Document

**Date:** 2026-05-12  
**Purpose:** Context document for continuing development with Claude Code  
**Language:** English (all repo work), Spanish (conversation with developer)  
**Repository:** https://github.com/Garsu26/hoem (private)

---

## 1. What HOEM is

Household coordination web application connecting five domains in a single value loop:
- **Pantry** — inventory, expiry alerts, low stock detection
- **Shopping** — auto-generated lists from low stock or weekly plan, real-time multi-member sync
- **Tasks** — assignment, recurrence, fair distribution among household members
- **Menu** — recipe library (global + per household), weekly plan, pantry-based suggestions
- **Energy** — ESIOS (Red Eléctrica) PVPC price cache, cheap/medium/expensive bands, appliance recommendations

**North Star Metric:** Weekly Active Households (WAH) coordinating ≥3 actions between 2+ members/week.

---

## 2. Tech Stack (fixed — do not change)

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3.x (8 microservices) |
| Frontend | React 19 + Vite + Zustand + TanStack Query |
| Database | PostgreSQL 16 — single instance, separate schemas per service |
| API Gateway | Spring Cloud Gateway |
| Migrations | Flyway — per service, `V1__init.sql` in `src/main/resources/db/migration/` |
| Inter-service HTTP | Spring Cloud OpenFeign + Resilience4j circuit breaker |
| Infrastructure | Docker + Docker Compose (`restart: unless-stopped` on all services) |
| Internet exposure | Cloudflare Tunnel (`cloudflared` container) |
| Email | Resend SDK for Java (`RESEND_API_KEY`) |
| Analytics | PostHog (`POSTHOG_API_KEY`) |
| API Docs | SpringDoc OpenAPI — auto-generated at `/actuator/docs` per service |
| Architecture tests | ArchUnit — enforces hexagonal architecture rules in CI |
| Style enforcement | Checkstyle (Google Java Style) + JaCoCo (70% coverage on domain + application layers) |

**Base Java package:** `dev.hoem`  
**All services run in WSL2 via Docker Desktop on Windows 11**

---

## 3. Repository Structure

```
hoem/                              ← github.com/Garsu26/hoem (private)
├── .github/
│   ├── ISSUE_TEMPLATE/            ← bug_report.md, feature_request.md, task.md
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── workflows/                 ← ci-auth.yml, ci-pantry.yml, ci-frontend.yml
├── docs/                          ← all 10 project documents (English)
├── scripts/
│   ├── init-schemas.sql           ← creates all 7 PostgreSQL schemas on first boot
│   └── seed-data.sql              ← global recipe catalogue + pantry categories
├── api-gateway/                   ← Spring Cloud Gateway (port 8080)
├── auth-service/                  ← port 8081
├── pantry-service/                ← port 8082
├── shopping-service/              ← port 8083
├── tasks-service/                 ← port 8084
├── menu-service/                  ← port 8085
├── energy-service/                ← port 8086
├── notification-service/          ← port 8087
├── frontend/                      ← React 19 + Nginx (port 3000)
├── docker-compose.yml             ← production
├── docker-compose.dev.yml         ← dev overrides (exposes individual service ports)
└── .env.example                   ← template — never commit .env
```

Each Java service follows **Hexagonal Architecture**:
```
src/main/java/dev/hoem/{service}/
├── controller/     ← REST + DTOs (input layer)
├── application/    ← use cases / application services
├── domain/         ← pure domain model (NO Spring, NO JPA, NO HTTP)
└── infrastructure/
    ├── persistence/  ← JPA repositories + entities
    ├── client/       ← Feign clients to other services
    └── external/     ← external API clients (ESIOS, Resend...)
```

**ArchUnit enforces:** `domain/` cannot import anything from Spring, JPA or infrastructure.

---

## 4. Database — 7 Schemas, 33 Tables

Full SQL in `docs/10_data_model.md`. Each service owns its schema exclusively.

| Schema | Service | Key tables |
|---|---|---|
| `auth` | auth-service | users, households, memberships, invitations, **join_requests**, sessions, verification_tokens |
| `pantry` | pantry-service | products, categories, stock_alerts |
| `shopping` | shopping-service | lists, items, list_snapshots |
| `tasks` | tasks-service | tasks, recurrence_rules, assignments, comments |
| `menu` | menu-service | recipes, recipe_steps, recipe_ingredients, meal_plans, meal_slots, recipe_tags |
| `energy` | energy-service | price_cache, daily_stats, alerts, alert_triggers, appliances, savings_log |
| `notifications` | notification-service | templates, notification_log, in_app_notifications, preferences |

**Critical design decisions:**
- `auth.households` has `invite_code CHAR(6)` and `invite_active BOOLEAN` — join flow requires admin approval via `join_requests` table
- `auth.memberships` has `color VARCHAR(7)` — hex color per user per household, used to colour-code tasks in the UI
- `energy.price_cache` uses `price_band VARCHAR(10)` with values `'cheap'`, `'medium'`, `'expensive'` (tertiles, NOT boolean)
- `energy.daily_stats` has three arrays: `cheap_hours INTEGER[]`, `medium_hours INTEGER[]`, `expensive_hours INTEGER[]`
- `menu.recipes` supports both global (`is_global = TRUE`, `household_id = NULL`) and per-household recipes
- `menu.recipe_steps` uses numbered steps (`step_number INTEGER`, `UNIQUE(recipe_id, step_number)`) — not free text
- No FK constraints between schemas — inter-service data integrity is enforced at application level, not DB level
- All PKs are `UUID` generated with `gen_random_uuid()`
- All timestamps are `TIMESTAMPTZ` (UTC storage, timezone conversion in frontend)

---

## 5. Architecture Decision Records (15 ADRs)

Full details in `docs/09_adr.md`. Critical ones for Sprint 1:

| ADR | Decision | Impact on code |
|---|---|---|
| ADR-003 | Stateless JWT at API Gateway | Gateway validates JWT locally with shared public key. Services receive `X-User-Id` and `X-Household-Id` as headers — never trust these from the client directly |
| ADR-004 | Single PostgreSQL, schemas per service | Each service connects with `?currentSchema=schema_name` in the JDBC URL |
| ADR-005 | Flyway per service | `V1__init.sql` in `src/main/resources/db/migration/` — runs automatically on startup |
| ADR-006 | Feign + Resilience4j | Every inter-service call needs a fallback method |
| ADR-007 | Conditional polling per screen | Frontend uses `useInterval` hook active only when component is mounted. Shopping list: 5s. Tasks: 15s. Others: load-only |
| ADR-011 | ArchUnit | `ArchitectureTest.java` in every service — template in repo root as `ArchitectureTest.java.template` |
| ADR-014 | `restart: unless-stopped` | Already in `docker-compose.yml` for all services |

---

## 6. Key Environment Variables

```bash
DB_USER=hoem
DB_PASSWORD=                    # strong password
JWT_SECRET=                     # min 32 chars — openssl rand -hex 32
RESEND_API_KEY=re_...           # resend.com
ESIOS_TOKEN=                    # api.esios.ree.es
CLOUDFLARE_TUNNEL_TOKEN=        # one.dash.cloudflare.com
POSTHOG_API_KEY=phc_...         # posthog.com
APP_BASE_URL=http://localhost:3000
```

---

## 7. Git Conventions

**Branch strategy:**
- `main` — production, protected, merge via PR only
- `develop` — integration branch, all features merge here
- `feature/description`, `fix/description`, `chore/description`, `docs/description`

**Commit format (Conventional Commits 1.0):**
```
feat(auth): add JWT refresh token endpoint
fix(pantry): correct expiry date timezone calculation
chore(docker): update postgres to 16.3
test(shopping): add integration tests for list snapshot
```

Types: `feat`, `fix`, `docs`, `chore`, `test`, `refactor`, `perf`, `ci`  
Scopes: `auth`, `pantry`, `shopping`, `tasks`, `menu`, `energy`, `notifications`, `frontend`, `gateway`, `docker`, `adr`

---

## 8. Current State — What is Done

### Documentation & infrastructure (unchanged)

| Item | Status |
|---|---|
| Vision document | ✅ `docs/00_vision.md` |
| PRD (Product Requirements) | ✅ `docs/01_prd.md` |
| User Stories (Gherkin AC) | ✅ `docs/02_user_stories.md` |
| Success Metrics (HEART + OKRs) | ✅ `docs/03_success_metrics.md` |
| Information Architecture | ✅ `docs/04_information_architecture.md` |
| User Flows (11 flows) | ✅ `docs/05_user_flows.md` |
| Wireframes spec (9 screens) | ✅ `docs/06_wireframes_spec.md` |
| C4 Architecture (levels 1–3) | ✅ `docs/08_c4_architecture.md` |
| 15 ADRs | ✅ `docs/09_adr.md` |
| Data model (7 schemas, 33 tables, Flyway V1) | ✅ `docs/10_data_model.md` |
| Repository structure | ✅ github.com/Garsu26/hoem |
| docker-compose.yml | ✅ all 10 containers defined |
| Dockerfiles (all services) | ✅ multi-stage Java + frontend Nginx |
| pom.xml (all services) | ✅ Spring Boot 3.3, Java 21, all base deps |
| GitHub Actions CI | ✅ auth, pantry, frontend workflows |
| GitHub issue/PR templates | ✅ |

### auth-service — Sprint 1 in progress (branch: `feat/auth-user-registration`)

**✅ Implemented — US-CORE-001 / FR-CORE-001 (2026-05-15, `mvn verify` green)**

`POST /api/v1/auth/register` — full hexagonal implementation:

```
auth-service/src/main/java/dev/hoem/auth/
├── AuthServiceApplication.java
├── controller/
│   ├── AuthController.java              POST /api/v1/auth/register → 201
│   ├── GlobalExceptionHandler.java      409 EMAIL_ALREADY_EXISTS · 400 PASSWORD_TOO_SHORT
│   └── dto/  RegisterRequest · RegisterResponse · ErrorResponse
├── application/
│   ├── command/  RegisterUserCommand
│   ├── result/   RegisterUserResult
│   ├── usecase/  RegisterUserUseCase
│   └── service/  RegisterUserService    orchestrates domain + ports
├── domain/
│   ├── model/    User · VerificationToken
│   ├── port/     UserRepository · VerificationTokenRepository · EmailService · PasswordHasher
│   └── exception/ EmailAlreadyExistsException
└── infrastructure/
    ├── persistence/  UserJpaEntity · VerificationTokenJpaEntity
    │                 UserJpaRepository · VerificationTokenJpaRepository (package-private)
    │                 UserRepositoryAdapter · VerificationTokenRepositoryAdapter
    ├── external/     ResendEmailServiceAdapter  (sends verification link to {APP_BASE_URL}/verify?token=)
    └── config/       AuthConfig (BCrypt cost 12 bean + PasswordHasher bean)
                      RateLimitFilter (Bucket4j — 10 req/min per IP on POST /api/v1/auth/register)

auth-service/src/main/resources/
├── application.yml                      (already existed — port 8081, currentSchema=auth)
└── db/migration/V1__init.sql            (already existed — all 7 auth tables)

auth-service/src/test/java/dev/hoem/auth/
├── ArchitectureTest.java                domain cannot import Spring/JPA/Hibernate
├── application/service/RegisterUserServiceTest.java   (Mockito unit tests)
└── controller/AuthControllerIT.java     (WebMvcTest slice — 201/409/400 scenarios)
```

**Dependencies added to auth-service/pom.xml:**
- `org.springframework.security:spring-security-crypto` — BCrypt (no full Security framework)
- `com.resend:resend-java:3.1.0` — Resend email SDK
- `com.bucket4j:bucket4j-core:8.10.1` — token-bucket rate limiting

**Key implementation decisions made (follow these patterns in future issues):**
- `RegisterRequest` uses `@Size(min=8)` on `password` field; `GlobalExceptionHandler` inspects field errors to return `PASSWORD_TOO_SHORT` vs generic `VALIDATION_ERROR`
- `PasswordHasher` is a domain port (interface in `domain/port/`), implemented in `infrastructure/config/AuthConfig` as an anonymous bean — keeps BCrypt out of domain
- `UserJpaRepository` and `VerificationTokenJpaRepository` are **package-private** — external code only sees the adapter
- `RateLimitFilter` reads `X-Forwarded-For` first, falls back to `remoteAddr`
- `ErrorResponse` is a simple record `{code, message}` — not RFC 9457 ProblemDetail (intentional for now)

**What is NOT yet in auth-service (pending issues):**
- Login (`POST /api/v1/auth/login`) + JWT access token issuance
- JWT refresh token (`POST /api/v1/auth/refresh`)
- Email verification (`GET /api/v1/auth/verify`)
- Household creation (`POST /api/v1/households`)
- Join request flow (`POST/PATCH /api/v1/households/{id}/join-requests`)
- Members endpoint (`GET /api/v1/households/{id}/members`)
- Password reset flow

**No source code exists yet in:** api-gateway, pantry-service, shopping-service, tasks-service, menu-service, energy-service, notification-service, frontend.

---

## 9. Next Steps — Sprint 1

**Goal:** `auth-service` fully functional + `pantry-service` basic CRUD + frontend shell with routing.

### Sprint 1 order (strict — each unblocks the next)

**Step 1 — `auth-service`** 🔄 In progress

- ✅ `POST /api/v1/auth/register` — user registration with email verification
- ⬜ `POST /api/v1/auth/login` — credentials validation, issue JWT access token (1h) + refresh token (30d stored in `auth.sessions`)
- ⬜ `POST /api/v1/auth/refresh` — rotate refresh token, issue new access token
- ⬜ `GET /api/v1/auth/verify` — consume `auth.verification_tokens`, set `users.verified = true`
- ⬜ `POST /api/v1/households` — create household + first membership (role = admin)
- ⬜ `POST /api/v1/households/{id}/join-requests` — user submits 6-char invite code
- ⬜ `PATCH /api/v1/households/{id}/join-requests/{requestId}` — admin approves/rejects
- ⬜ `GET /api/v1/households/{id}/members` — list members with color map

**JWT notes for login issue:** `JWT_SECRET` is shared between auth-service and api-gateway. auth-service signs the token; api-gateway validates it. Use `io.jsonwebtoken:jjwt-*` (add to pom.xml) or `com.nimbusds:nimbus-jose-jwt`. Token payload must include `sub` (userId), `householdId`, and `exp`.

---

**Step 2 — `api-gateway`** (blocked until login + JWT are done)
- Spring Cloud Gateway routing — routes all `/api/v1/**` to the right service by prefix
- `JwtAuthenticationFilter` — validates JWT locally using `JWT_SECRET`, rejects 401 if invalid/expired
- Injects `X-User-Id` and `X-Household-Id` headers into downstream request after validation
- Whitelist (no JWT required): `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `GET /api/v1/auth/verify`
- CORS for `http://localhost:3000`
- No `src/main/java` exists yet — start from scratch following same hexagonal layout (filter in `infrastructure/`)

---

**Step 3 — `pantry-service`** (blocked until api-gateway is done)
- Domain: `Product`, `Category`
- Application: `AddProductUseCase`, `UpdateProductUseCase`, `GetPantryUseCase`, `DeleteProductUseCase`
- Controller reads `X-Household-Id` header (never JWT) — standard for all services
- Feign client to auth-service for member validation (with `@FallbackFactory`)
- Full CRUD: `GET/POST /api/v1/pantry/products`, `PUT/DELETE /api/v1/pantry/products/{id}`
- Flyway `V1__init.sql` — pantry schema from `docs/10_data_model.md`

---

**Step 4 — Frontend shell** (blocked until api-gateway is done)
- Vite + React 19 project init
- React Router v6 with all routes from `docs/04_information_architecture.md`
- Zustand stores: `useAuthStore` (tokens, user), `useHouseholdStore` (active household, members color map)
- TanStack Query for all server state
- Authenticated layout with sidebar/bottom nav
- Auth pages: register, login, email verification, household onboarding
- Pantry page connected to real API

---

## 10. pom.xml Template Notes

The `pom.xml.template` in the repo root is the base for all services. When creating a service:
1. Copy to `{service-name}/pom.xml`
2. Change `<artifactId>service-template</artifactId>` to `<artifactId>{service-name}</artifactId>`
3. Change `<description>` accordingly
4. The `application.yml.template` in repo root is the base — copy to `src/main/resources/application.yml` and set `spring.application.name` and `server.port`

---

## 11. Important Context for Claude Code

### Things to always remember
- **No FK constraints between schemas** — if you see a FK referencing another schema's table, remove it
- **`domain/` layer must be pure Java** — no `@Entity`, no `@Repository`, no Spring annotations. JPA entities live in `infrastructure/persistence/`
- **ArchUnit test must pass** — write it early, not at the end
- **`restart: unless-stopped`** is already in docker-compose — do not remove it
- **JWT secret is shared** — api-gateway and auth-service share `JWT_SECRET` env var
- **Services receive `X-User-Id` and `X-Household-Id` from the gateway** — never read JWT in individual services
- **Flyway manages all schema changes** — `spring.jpa.hibernate.ddl-auto=validate` in all services, never `create` or `update`
- **Test coverage minimum 70%** on `application/` and `domain/` packages — enforced by JaCoCo in `mvn verify`
- **Google Java Style** enforced by Checkstyle — run `mvn checkstyle:check` before committing

### Things NOT to do
- Do not add Redis — not in the architecture (YAGNI, ADR decision)
- Do not add RabbitMQ or Kafka — synchronous HTTP is sufficient for MVP
- Do not use WebSockets — polling is the decision for v1 (ADR-007). WebSockets documented for v2
- Do not use `@Transactional` across services — transactions are per-service only
- Do not hardcode `household_id` or `user_id` — always read from `X-Household-Id` / `X-User-Id` headers
- Do not use `spring.jpa.hibernate.ddl-auto=create` or `update` — Flyway only
- Do not create FKs referencing tables in other schemas
- Do not commit `.env` — it is in `.gitignore`

### ESIOS API (energy-service only)
- Base URL: `https://api.esios.ree.es`
- Indicator for PVPC: `1001`
- Requires `Authorization: Token token="{ESIOS_TOKEN}"` header
- Prices change every hour — cache with `@Scheduled(cron = "0 0 * * * *")`
- Price band classification: tertiles of the 24 daily prices → cheapest 8h = `cheap`, middle 8h = `medium`, most expensive 8h = `expensive`
- Fallback: if API unavailable, serve cached data with `data_stale` flag. Do NOT send alerts if data is >25h old

### Shopping list polling (shopping-service)
- `GET /api/v1/shopping-lists/{id}/snapshot` returns `{ list_id, updated_at, items_total, items_checked }`
- Frontend polls this every 5s when the list screen is open
- Only fetches full list if `updated_at` changed since last poll
- Respond `304 Not Modified` if nothing changed

### Household invite flow (auth-service)
- `auth.households.invite_code` is `CHAR(6)`, auto-generated, uppercase
- User submits code → creates `join_requests` record with `status = 'pending'`
- Admin approves → `status = 'accepted'` + new `memberships` record created
- Admin rejects → `status = 'rejected'`
- Admin can disable code: `invite_active = FALSE`

### Member color (auth-service + frontend)
- `auth.memberships.color VARCHAR(7)` stores hex color (e.g. `#3B82F6`)
- Default: `#6366F1`
- Frontend builds a `{ user_id → color }` map from the members endpoint
- Used to colour-code task cards in the tasks module

---

## 12. Developer Notes

- **Language in conversation:** Spanish (developer preference)
- **Language in code/repo/commits/docs:** English (all code, comments, variable names, commit messages, PR descriptions, wiki)
- **Working hours:** part-time, ~10–15h/week
- **Experience level:** computer science student — explain standards and patterns the first time they appear in a new context
- **IDE:** likely IntelliJ IDEA (Java) — generate standard IntelliJ project structure
- **OS:** Windows 11 with WSL2 + Docker Desktop
- **Git:** two GitHub accounts configured via SSH aliases — personal (`github-personal`) for HOEM, university (`github-uni`) for academic repos
- **All backend is Java** — no Node.js microservices (decision made and documented)

---

## 13. Files Ready to Copy into Services

These templates are in the repo root — copy and adapt when creating each service:

| Template file | Destination | Changes needed |
|---|---|---|
| `pom.xml.template` | `{service}/pom.xml` | Change `artifactId` and `description` |
| `application.yml.template` | `{service}/src/main/resources/application.yml` | Set `spring.application.name` and `server.port` |
| `Dockerfile.template` | `{service}/Dockerfile` | None — works as-is |
| `ArchitectureTest.java.template` | `{service}/src/test/java/dev/hoem/{service}/ArchitectureTest.java` | Replace all `SERVICE_NAME` occurrences |

The `V1__init.sql` for each service is in `docs/10_data_model.md` — each schema section has its own migration block clearly labelled.

---

*Created: 2026-05-12 — end of planning phase*  
*Updated: 2026-05-15 — auth-service registration complete, Sprint 1 in progress*
