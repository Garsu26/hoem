# HOEM — C4 Architecture

**Version:** 1.1
**Date:** 2026-05-12
**Status:** Approved — pre-MVP
**Standards applied:**
- C4 Model (Simon Brown, c4model.com) — levels 1 to 3
- ISO/IEC/IEEE 42010:2022 — Architecture description
- Domain-Driven Design / Bounded Contexts (Eric Evans, 2003)
- Hexagonal Architecture / Ports & Adapters (Alistair Cockburn, 2005)

---

## 1. Architectural Constraints (ASR)

These are the requirements that shape the system. If any of them changes, the architecture is re-evaluated.

| ID | Constraint | Implication |
|---|---|---|
| C-001 | Java 21 + Spring Boot 3.x + React 19 + PostgreSQL 16 | Fixed stack. All backend is Java |
| C-002 | Home server Windows 11 + WSL2 + Docker Desktop | Containers run in WSL2. No cold starts. No invocation limits |
| C-003 | Cloudflare Tunnel for internet exposure | Automatic HTTPS, no open router ports, DDNS included |
| C-004 | Zero budget | Permanently free services only |
| C-005 | Single developer | Microservices yes, but with simple docker-compose. No Kubernetes |
| C-006 | 13 GB RAM available for containers | JVM limited to -Xmx128m per Spring Boot service |

---

## 2. C1 View — System Context

Shows HOEM as a black box with its actors and external systems.

```
┌─────────────────────────────────────────────────────────────────┐
│                         INTERNET                                │
│                                                                 │
│  [User/Browser] ──► [Cloudflare Tunnel] ──► [HOEM]             │
│                                                                 │
│  HOEM connects to:                                              │
│  · ESIOS API (Red Eléctrica) — electricity prices, free         │
│  · Resend API — transactional email (3,000/month free)          │
│  · PostHog — product analytics (1M events/month free)           │
└─────────────────────────────────────────────────────────────────┘
```

### Actors

| Actor | Role | Access |
|---|---|---|
| Visitor | Anonymous user | Landing, login, registration |
| Member | Authenticated user with role `member` | All 5 functional modules |
| Admin | Authenticated user with role `admin` | All 5 modules + household management |

### External Systems

| System | Purpose | Cost | Criticality |
|---|---|---|---|
| ESIOS API (REE) | Real-time PVPC prices and 24h predictions | Free, public | High — energy module depends on it |
| Resend | Transactional email (verification, invitations, reminders) | Free up to 3,000/month | Medium — app can degrade without email |
| PostHog | Product analytics (AARRR/HEART events) | Free up to 1M events/month | Low — does not affect product |
| Cloudflare Tunnel | HTTPS exposure without opening ports | Free forever | High — it is the entry point |

### What is NOT in the C1 (and why)

| Excluded | Reason |
|---|---|
| Payment gateways | Revenue = 0 in MVP |
| Supermarket APIs | Out of v1 scope (PRD §8) |
| OAuth / social login | Not in v1. Email + password is sufficient for MVP |
| Push notifications | Requires PWA with service worker; email covers MVP |

---

## 3. C2 View — Containers

Zoom inside HOEM. Each container is an independent Docker process.

### 3.1 Container diagram

```
┌─────────────────────── Your computer (Docker) ───────────────────────────┐
│                                                                           │
│  [Cloudflare Tunnel :443] ──► [API Gateway :8080]                         │
│                                       │                                   │
│              ┌────────────────────────┼────────────────────────┐          │
│              ▼                        ▼                        ▼          │
│    [auth-service :8081]   [pantry-service :8082]  [shopping-service :8083]│
│                                                                           │
│    [tasks-service :8084]  [menu-service :8085]    [energy-service :8086]  │
│                                                                           │
│    [notification-service :8087]   [frontend :3000]                        │
│                                                                           │
│    [PostgreSQL :5432]     [Cloudflare R2 — images]                        │
│                                                                           │
└───────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Container inventory

| Container | Technology | Port | Responsibility |
|---|---|---|---|
| `api-gateway` | Spring Cloud Gateway | 8080 | Single entry point. Routing, JWT auth, rate limiting, CORS |
| `auth-service` | Spring Boot 3 | 8081 | Registration, login, JWT, password recovery, household and member management |
| `pantry-service` | Spring Boot 3 | 8082 | Product management, categories, low stock, expiry alerts |
| `shopping-service` | Spring Boot 3 | 8083 | Shopping lists, items, real-time synchronisation (polling) |
| `tasks-service` | Spring Boot 3 | 8084 | Tasks, recurrence, assignments, round-robin |
| `menu-service` | Spring Boot 3 | 8085 | Recipes, weekly plan, rule-based suggestions, list generation from plan |
| `energy-service` | Spring Boot 3 | 8086 | ESIOS integration, price cache, rate alerts, recommendations |
| `notification-service` | Spring Boot 3 | 8087 | Email sending via Resend. Consumes internal events from other services |
| `frontend` | React 19 + Nginx | 3000 | SPA served by Nginx. Communicates only with the API Gateway |
| `postgresql` | PostgreSQL 16 | 5432 | Single DB with separate schemas per service |

### 3.3 PostgreSQL schemas per service

Each service has its own schema. A service can only access its own schema, never another's. If it needs data from another service, it calls it via HTTP.

| Schema | Owning service | Main tables |
|---|---|---|
| `auth` | auth-service | users, households, memberships, invitations, join_requests, sessions, verification_tokens |
| `pantry` | pantry-service | products, categories, stock_alerts |
| `shopping` | shopping-service | lists, items, list_snapshots |
| `tasks` | tasks-service | tasks, assignments, recurrence_rules, comments |
| `menu` | menu-service | recipes, recipe_steps, recipe_ingredients, meal_plans, meal_slots, recipe_tags |
| `energy` | energy-service | price_cache, daily_stats, alerts, alert_triggers, appliances, savings_log |
| `notifications` | notification-service | notification_log, templates, in_app_notifications, preferences |

### 3.4 Why NOT these additional containers

| Discarded | Why not |
|---|---|
| Redis | YAGNI. List synchronisation solved with polling + timestamps. Added if metrics justify it |
| RabbitMQ / Kafka | Message volume between services in MVP does not justify a broker. Synchronous HTTP is sufficient |
| Elasticsearch | Simple ILIKE search in PostgreSQL covers MVP |
| Kubernetes | docker-compose is sufficient for a home server with one developer |
| Zipkin / Jaeger | Distributed tracing useful but not critical in MVP. Structured logs per service are sufficient |

---

## 4. C3 View — Components (inside each Spring Boot service)

### 4.1 Internal structure of each service

All services follow the same layered architecture with Hexagonal Architecture:

```
[service-x]
├── controller/          → REST controllers (HTTP input layer)
│   └── dto/             → Request/Response DTOs + validation (@Valid)
├── application/         → Use cases / application logic
│   └── service/         → Application services (orchestrate domain)
├── domain/              → Entities, value objects, domain exceptions
│   └── model/           → Pure domain classes (no Spring, no JPA)
├── infrastructure/      → Concrete implementations
│   ├── persistence/     → JPA repositories + JPA entities
│   ├── client/          → Feign clients to call other services
│   └── external/        → External API clients (ESIOS, Resend…)
└── config/              → Spring configuration (Security, Feign, etc.)
```

**Dependency rules (enforced by ArchUnit in CI):**

- `domain/` does not depend on anything external. No Spring, no JPA, no HTTP.
- `application/` depends on `domain/` and interfaces defined in `domain/`.
- `infrastructure/` implements the interfaces from `domain/`.
- `controller/` only calls `application/`. Never touches `infrastructure/` directly.

### 4.2 Repository folder structure

```
hoem/
├── docker-compose.yml           ← brings up the entire system
├── docker-compose.dev.yml       ← overrides for local development
├── .env.example                 ← environment variables (never commit .env)
│
├── docs/                        ← all project documentation (this folder)
│
├── scripts/
│   ├── init-schemas.sql         ← creates all PostgreSQL schemas
│   └── seed-data.sql            ← initial data (global recipe catalogue)
│
├── api-gateway/
│   ├── src/
│   └── Dockerfile
│
├── auth-service/
│   ├── src/main/java/dev/hoem/auth/
│   │   ├── controller/
│   │   ├── application/
│   │   ├── domain/
│   │   └── infrastructure/
│   │       └── persistence/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       └── V1__init.sql
│   ├── src/test/
│   ├── pom.xml
│   └── Dockerfile
│
├── pantry-service/              ← same structure
├── shopping-service/            ← same structure
├── tasks-service/               ← same structure
├── menu-service/                ← same structure
├── energy-service/              ← same structure
├── notification-service/        ← same structure
│
├── frontend/
│   ├── src/
│   │   ├── pages/               ← React Router pages
│   │   ├── components/          ← reusable components
│   │   ├── hooks/               ← custom hooks (useInterval, useAuth…)
│   │   ├── services/            ← API Gateway calls
│   │   └── store/               ← global state (Zustand)
│   ├── nginx.conf
│   ├── package.json
│   └── Dockerfile
│
└── shared/
    └── proto/                   ← OpenAPI contracts per service (if used)
```

---

## 5. Cross-Cutting Views

### 5.1 Authentication and authorisation

```
User → API Gateway → auth-service (POST /auth/login)
                   ← JWT (access token 1h + refresh token 30d)

Subsequent request:
User → API Gateway [validates JWT locally with public key]
     → destination service [receives userId + householdId in header]
```

**Decisions:**

- **Stateless JWT at the API Gateway.** The gateway validates the token signature without calling auth-service on each request. Minimum latency.
- **BCrypt cost 12** for password hashing in auth-service.
- **Refresh token in DB** (table `sessions` in schema `auth`). Allows revoking sessions individually.
- **Active householdId** travels in the JWT claims. The user can switch household by logging in again or calling a swap endpoint.
- Each service receives `X-User-Id` and `X-Household-Id` as headers injected by the gateway. Never trusts headers coming directly from the client.

### 5.2 Inter-service communication

**General rule: synchronous HTTP via Feign Client** for inter-service calls in MVP.

```
menu-service ──Feign──► pantry-service
                         GET /api/v1/pantry?householdId={id}
                         ← List<Product>
```

- **Resilience4j** wraps every Feign call with circuit breaker + retry.
- **Fallback:** if `pantry-service` does not respond, `menu-service` returns suggestions without pantry filtering, with a note "pantry data unavailable".
- **No message broker in MVP.** Asynchronous communication added when data justifies it.

### 5.3 Shopping list synchronisation (ADR-007)

```
Member A (actor)    →  PATCH /api/v1/shopping-lists/{id}/items/{itemId}
                    ←  200 OK
                    →  Optimistic UI: crossed out immediately

Member B (observer) →  GET /api/v1/shopping-lists/{id}/snapshot  (every 5s)
                    ←  { updated_at, items_total, items_checked }
                    →  If updated_at changed → GET full list
                    ←  Updated list (≤ 5s latency)
```

### 5.4 Energy price cache (ADR-010)

```
@Scheduled(cron = "0 0 * * * *")   ← every hour
energy-service → GET api.esios.ree.es/indicators/1001
               → Calculate cheap/medium/expensive bands (tertiles)
               → INSERT INTO energy.price_cache
               → UPDATE energy.daily_stats
               → Evaluate active alerts → POST notification-service
```

---

## 6. docker-compose.yml (skeleton)

```yaml
version: '3.9'

services:
  postgresql:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=hoem
      - POSTGRES_USER=${DB_USER}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-schemas.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 10s
      retries: 5

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - AUTH_SERVICE_URL=http://auth-service:8081
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  auth-service:
    build: ./auth-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=auth
      - SPRING_DATASOURCE_USERNAME=${DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - RESEND_API_KEY=${RESEND_API_KEY}
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  pantry-service:
    build: ./pantry-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=pantry
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  shopping-service:
    build: ./shopping-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=shopping
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  tasks-service:
    build: ./tasks-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=tasks
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  menu-service:
    build: ./menu-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=menu
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  energy-service:
    build: ./energy-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=energy
      - ESIOS_TOKEN=${ESIOS_TOKEN}
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  notification-service:
    build: ./notification-service
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/hoem?currentSchema=notifications
      - RESEND_API_KEY=${RESEND_API_KEY}
      - JAVA_OPTS=-Xmx128m -Xms64m
    depends_on:
      postgresql:
        condition: service_healthy
    restart: unless-stopped

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - api-gateway
    restart: unless-stopped

volumes:
  postgres_data:
```

**Full system startup:**
```bash
cp .env.example .env        # fill in variables
docker-compose up -d        # start everything in background
docker-compose logs -f      # view logs in real time
docker-compose down         # stop everything (data persists in volume)
docker-compose down -v      # stop everything AND delete data
```

---

## 7. Estimated RAM Consumption

With `-Xmx128m -Xms64m` per Java service and WSL2 as Docker Desktop backend:

| Container | Estimated RAM at rest |
|---|---|
| Windows 11 + WSL2 + Docker Desktop | ~3,000 MB |
| api-gateway (Spring Cloud) | ~200 MB |
| auth-service | ~180 MB |
| pantry-service | ~150 MB |
| shopping-service | ~150 MB |
| tasks-service | ~150 MB |
| menu-service | ~150 MB |
| energy-service | ~150 MB |
| notification-service | ~130 MB |
| frontend (Nginx) | ~30 MB |
| postgresql | ~200 MB |
| **Estimated total** | **~4,700 MB** |
| **Available (13 GB)** | **~8,300 MB margin** |

The margin is ample. Under real load of 30–100 users, Spring Boot services rarely exceed 256 MB each.

---

## 8. Architecture Decision Records (ADR Index)

Full ADR details in `09_adr.md`.

| # | Decision | Summary |
|---|---|---|
| ADR-001 | Microservices from the start | Developer masters the stack; separation from day one avoids costly rewrite |
| ADR-002 | Spring Cloud Gateway as API Gateway | Spring ecosystem standard. Routing, JWT validation, rate limiting in one place |
| ADR-003 | Stateless JWT at the gateway | Validates without calling auth-service on each request. Public key shared via env var |
| ADR-004 | Single PostgreSQL, separate schemas | Same logical isolation without the operational cost of multiple instances |
| ADR-005 | Flyway per service | Each service manages its own migrations independently |
| ADR-006 | Synchronous HTTP between services (Feign + Resilience4j) | Sufficient for MVP volume. No broker until data justifies it |
| ADR-007 | Conditional polling per screen for collaborative sync | Stateless services, no Gateway/Cloudflare config needed. WebSockets documented for v2 |
| ADR-008 | Cloudflare Tunnel for exposure | Permanently free, automatic HTTPS, DDNS, no open ports |
| ADR-009 | Resend for transactional email | Free up to 3,000 emails/month, clean API, DKIM/SPF managed |
| ADR-010 | Hourly ESIOS price cache | Prices change every hour. Cache avoids real-time dependency and covers fallback |
| ADR-011 | ArchUnit to enforce dependency rules | Prevents domain from importing Spring or JPA. Rules checked in CI automatically |
| ADR-012 | Zustand for global state in React | Lighter than Redux for a SPA of this size. No unnecessary boilerplate |
| ADR-013 | SpringDoc OpenAPI per service | Auto-generated documentation at `/actuator/docs`. No manual maintenance |
| ADR-014 | Docker restart policy `unless-stopped` | Automatic recovery after container crashes and Windows restarts |
| ADR-015 | Logs per service with `docker-compose logs` | No extra containers or RAM. Sufficient for MVP with a single developer |

---

## 9. Open Decisions

These decisions must be closed before starting each module:

1. **OpenAPI specs:** SpringDoc per service publishing at `/actuator/docs`. No centralised contract yet. ✅ Closed — ADR-013.
2. **Automatic startup on server restart:** Docker `restart: unless-stopped` policy. ✅ Closed — ADR-014.
3. **Logs:** per service via `docker-compose logs` in MVP. ✅ Closed — ADR-015.
4. **Energy module in frontend:** energy widget on dashboard + own section `/app/energy`. ✅ Closed — reflected in wireframes WF-02 and WF-05/06.

---

## Changelog

| Version | Date | Changes |
|---|---|---|
| 0.1 | 2026-05-10 | Original architecture (Next.js, modular monolith, Vercel+Neon) |
| 1.0 | 2026-05-10 | Full rewrite: Java/Spring Boot, microservices, Docker, home server, energy module |
| 1.1 | 2026-05-12 | Translated to English. All 4 open decisions closed. ADR index expanded to 15 (ADR-013/014/015 added). `restart: unless-stopped` added to docker-compose skeleton. `join_requests` table added to auth schema inventory |
