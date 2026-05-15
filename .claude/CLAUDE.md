# HOEM — Claude Code Context

This file is read automatically by Claude Code on every session.
**Conversation language: Spanish. Code/commits/docs language: English.**

---

## What is this project

Household coordination web app connecting five domains: pantry, shopping, tasks, weekly menu and energy optimisation. Full product documentation in `docs/`.

**Repository:** https://github.com/Garsu26/hoem (private)  
**Developer:** single developer, part-time (~10–15h/week), computer science student  
**OS:** Windows 11 + WSL2 + Docker Desktop  

---

## Stack (fixed — do not suggest alternatives)

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3.x — 8 microservices |
| Frontend | React 19 + Vite + Zustand + TanStack Query |
| Database | PostgreSQL 16 — single instance, 7 schemas |
| Gateway | Spring Cloud Gateway (port 8080) |
| Migrations | Flyway per service — `V1__init.sql` in `src/main/resources/db/migration/` |
| Inter-service | Spring Cloud OpenFeign + Resilience4j |
| Infrastructure | Docker Compose — `restart: unless-stopped` on all services |
| Exposure | Cloudflare Tunnel (`cloudflared` container) |
| Email | Resend SDK for Java |
| Analytics | PostHog |
| API docs | SpringDoc OpenAPI at `/actuator/docs` per service |
| Arch tests | ArchUnit — enforces hexagonal architecture |
| Coverage | JaCoCo — minimum 70% on `domain/` and `application/` layers |
| Style | Checkstyle — Google Java Style Guide |

**Base Java package:** `dev.hoem`

---

## Service ports

| Service | Port |
|---|---|
| api-gateway | 8080 |
| auth-service | 8081 |
| pantry-service | 8082 |
| shopping-service | 8083 |
| tasks-service | 8084 |
| menu-service | 8085 |
| energy-service | 8086 |
| notification-service | 8087 |
| frontend | 3000 |
| postgresql | 5432 |

---

## Internal architecture — every Java service

```
src/main/java/dev/hoem/{service}/
├── controller/        ← REST controllers + DTOs (input layer)
├── application/       ← use cases / application services
├── domain/            ← pure Java — NO Spring, NO JPA, NO HTTP
└── infrastructure/
    ├── persistence/   ← JPA entities + repositories
    ├── client/        ← Feign clients to other services
    └── external/      ← external API clients (ESIOS, Resend)
```

**ArchUnit rule (enforced in CI):** `domain/` cannot import anything from `org.springframework`, `jakarta.persistence` or `org.hibernate`. Template: `ArchitectureTest.java.template` in repo root.

**JPA entities live in `infrastructure/persistence/`** — never in `domain/`.  
**Domain model is pure Java** — plain classes with no framework annotations.

---

## Database rules

- **No FK constraints between schemas** — inter-service integrity at application level only
- **`spring.jpa.hibernate.ddl-auto=validate`** in all services — Flyway manages schema, never Hibernate
- **All PKs:** `UUID` generated with `gen_random_uuid()`
- **All timestamps:** `TIMESTAMPTZ` (UTC storage)
- Each service connects with `?currentSchema={schema}` in the JDBC URL

### Schema → service mapping

| Schema | Service |
|---|---|
| `auth` | auth-service |
| `pantry` | pantry-service |
| `shopping` | shopping-service |
| `tasks` | tasks-service |
| `menu` | menu-service |
| `energy` | energy-service |
| `notifications` | notification-service |

Full SQL schema in `docs/10_data_model.md`.

---

## Critical design decisions (full ADRs in `docs/09_adr.md`)

### Authentication (ADR-003)
- API Gateway validates JWT **locally** using shared public key (`JWT_SECRET` env var)
- Gateway injects `X-User-Id` and `X-Household-Id` headers into every downstream request
- Individual services **never** parse JWT — they only read these headers
- Access token expiry: 1h. Refresh token: 30d (stored in `auth.sessions`, revocable)
- BCrypt cost 12 for password hashing

### Shopping list sync (ADR-007)
- **Polling only — no WebSockets** (documented for v2)
- `GET /api/v1/shopping-lists/{id}/snapshot` → `{ list_id, updated_at, items_total, items_checked }`
- Frontend polls every 5s when list screen is mounted (`useInterval` hook)
- Full list fetched only if `updated_at` changed — respond `304 Not Modified` otherwise
- Optimistic UI: actor sees change immediately, observers in ≤5s

### Energy price bands (ADR-010)
- ESIOS API: `GET https://api.esios.ree.es/indicators/1001`
- Header required: `Authorization: Token token="{ESIOS_TOKEN}"`
- Cached hourly via `@Scheduled(cron = "0 0 * * * *")`
- Price band = tertiles of 24 daily prices: cheapest 8h → `cheap`, middle 8h → `medium`, most expensive 8h → `expensive`
- `price_band VARCHAR(10)` in `energy.price_cache` — NOT a boolean
- Three arrays in `energy.daily_stats`: `cheap_hours INTEGER[]`, `medium_hours INTEGER[]`, `expensive_hours INTEGER[]`
- Fallback: serve cached data with warning if API unavailable. No alerts if data >25h old

### Household invite flow
- `auth.households.invite_code CHAR(6)` — uppercase, auto-generated, unique
- `auth.households.invite_active BOOLEAN` — admin can disable without regenerating
- User submits code → `join_requests` record with `status = 'pending'`
- Admin approves → `status = 'accepted'` + new `memberships` row
- Admin rejects → `status = 'rejected'`

### Member colour
- `auth.memberships.color VARCHAR(7)` — hex colour per user per household (default `#6366F1`)
- Frontend builds `{ user_id → color }` map from members endpoint
- Used to colour-code task cards in the tasks UI

### Inter-service calls (ADR-006)
- Feign client + Resilience4j circuit breaker on every call
- Always define a `@FallbackFactory` — service must degrade gracefully if dependency is down
- No message broker (no RabbitMQ, no Kafka) — synchronous HTTP only in MVP

---

## Things to NEVER do

- ❌ Add Redis — not in architecture (YAGNI, decided and documented)
- ❌ Add RabbitMQ or Kafka — synchronous HTTP sufficient for MVP
- ❌ Add WebSockets — polling is the v1 decision (ADR-007)
- ❌ Use `spring.jpa.hibernate.ddl-auto=create` or `update` — Flyway only
- ❌ Create FK constraints referencing tables in other schemas
- ❌ Import Spring/JPA annotations in `domain/` package
- ❌ Read JWT in individual services — only read `X-User-Id` / `X-Household-Id` headers
- ❌ Use `@Transactional` across service boundaries
- ❌ Hardcode `household_id` or `user_id` — always from request headers
- ❌ Commit `.env` file — it is in `.gitignore`
- ❌ Use `System.out.println` — use SLF4J logger (`private static final Logger log = LoggerFactory.getLogger(...)`)
- ❌ Expose domain entities directly from controllers — always use DTOs

---

## Git conventions

**Branches:** `main` (protected) ← `develop` ← `feature/`, `fix/`, `chore/`, `docs/`  
**Commits:** Conventional Commits 1.0

```
feat(auth): add JWT refresh token endpoint
fix(pantry): correct expiry date timezone calculation
chore(docker): update postgres image to 16.3
test(shopping): add integration tests for list snapshot
refactor(energy): extract price band calculation to domain service
```

**Scopes:** `auth`, `pantry`, `shopping`, `tasks`, `menu`, `energy`, `notifications`, `frontend`, `gateway`, `docker`, `adr`, `docs`

---

## Templates in repo root

| File | Use |
|---|---|
| `pom.xml.template` | Base `pom.xml` for every Java service — change `artifactId` |
| `application.yml.template` | Base `application.yml` — set `spring.application.name` and `server.port` |
| `Dockerfile.template` | Multi-stage Dockerfile for every Java service |
| `ArchitectureTest.java.template` | ArchUnit test — replace `SERVICE_NAME` throughout |

---

## Current sprint — Sprint 1

**Goal:** auth-service working + pantry-service basic CRUD + frontend shell

### Build order for Sprint 1 (strict)
1. ~~`auth-service`~~ ✅ Done — 2026-05-15
2. `api-gateway` — needed to test auth from frontend  ← **next**
3. `pantry-service` — first functional module
4. Frontend shell — routing, auth pages, pantry page connected to real API

### What exists

**Infrastructure (all services):**
- `docker-compose.yml` with all 10 containers defined
- `Dockerfile` in every service directory (multi-stage)
- `pom.xml` in every service directory (Spring Boot 3.3, Java 21)
- `docs/` with all 10 project documents
- `scripts/init-schemas.sql` and `scripts/seed-data.sql`

**auth-service — fully implemented:**
- `POST /api/v1/auth/register` → 201, 409 `EMAIL_ALREADY_EXISTS`, 400 `PASSWORD_TOO_SHORT`
- BCrypt cost 12 (`spring-security-crypto`), verification email via Resend SDK (`resend-java:3.1.0`)
- Rate limiting 10 req/min per IP via Bucket4j (`bucket4j-core:8.10.1`)
- Hexagonal architecture: `domain/` (User, VerificationToken, ports, exception), `application/` (RegisterUserService), `controller/` (AuthController, GlobalExceptionHandler, DTOs), `infrastructure/` (JPA adapters, ResendEmailServiceAdapter, AuthConfig, RateLimitFilter)
- `application.yml`, `V1__init.sql` (7 auth tables), `ArchitectureTest.java`, unit + controller slice tests
- `mvn verify` passes — Checkstyle, JaCoCo ≥70%, all tests green

### What does NOT exist yet
- No Java source files in api-gateway, pantry-service, or any other service
- No React source files
- No `application.yml` outside auth-service
- No `ArchitectureTest.java` outside auth-service

---

## Running the project

```bash
# Start everything
docker-compose up -d

# Start only what you need
docker-compose up -d postgresql api-gateway auth-service frontend

# Logs for a specific service
docker-compose logs -f auth-service

# Restart after code change
docker-compose restart auth-service

# Run tests for a Java service
cd auth-service && mvn test

# Run tests with coverage
cd auth-service && mvn verify
```

---

## Environment variables (see .env.example)

```
DB_USER, DB_PASSWORD
JWT_SECRET           (min 32 chars)
RESEND_API_KEY       (re_...)
ESIOS_TOKEN
CLOUDFLARE_TUNNEL_TOKEN
POSTHOG_API_KEY      (phc_...)
APP_BASE_URL         (http://localhost:3000 in dev)
```
