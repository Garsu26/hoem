# Contributing to HOEM

This document covers the conventions and workflows for developing HOEM.

---

## Commit Convention

HOEM uses [Conventional Commits 1.0](https://www.conventionalcommits.org/). Every commit must follow this format:

```
type(scope): short description in lowercase

[optional body]

[optional footer: Closes #issue]
```

### Types

| Type | When to use |
|---|---|
| `feat` | New feature or behaviour |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `chore` | Build, config, dependencies — no production code change |
| `test` | Adding or fixing tests |
| `refactor` | Code change that is neither a fix nor a feature |
| `perf` | Performance improvement |
| `ci` | CI/CD pipeline changes |

### Scopes

Use the service or module name: `auth`, `pantry`, `shopping`, `tasks`, `menu`, `energy`, `notifications`, `frontend`, `gateway`, `docker`, `adr`, `docs`.

### Examples

```
feat(auth): add JWT refresh token endpoint
fix(pantry): correct expiry date calculation for timezone edge case
docs(adr): add ADR-007 conditional polling decision
chore(docker): update postgres image to 16.3
test(shopping): add integration tests for list snapshot endpoint
refactor(energy): extract price band calculation to domain service
ci(auth): add ArchUnit test step to workflow
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Production. Protected. Merge via PR only |
| `develop` | Integration branch. All features merge here first |
| `feature/description` | New feature or user story |
| `fix/description` | Bug fix |
| `chore/description` | Technical task, config, deps |
| `docs/description` | Documentation update |

### Branch naming examples

```
feature/auth-refresh-token
feature/pantry-expiry-alerts
fix/shopping-polling-null-pointer
chore/update-spring-boot-3.3
docs/update-adr-007
```

---

## Pull Request Process

1. Branch off from `develop` (never from `main` directly).
2. Keep PRs small and focused — one feature or fix per PR.
3. Fill in the PR template completely.
4. All CI checks must pass before merging.
5. Squash and merge to keep a clean `develop` history.
6. Link the PR to its issue with `Closes #N` in the description.

---

## Local Development

```bash
# Start all services
docker-compose up -d

# Start only the services you are working on
docker-compose up -d postgresql api-gateway auth-service frontend

# View logs for a specific service
docker-compose logs -f auth-service

# Restart a single service after code change
docker-compose restart auth-service

# Run tests for a Java service
cd auth-service && mvn test

# Run frontend in dev mode (hot reload)
cd frontend && npm run dev
```

---

## Code Standards

### Java (all backend services)

- **Architecture:** Hexagonal (Ports & Adapters). `domain/` has zero Spring/JPA imports — enforced by ArchUnit.
- **Style:** Google Java Style Guide. Enforced by Checkstyle in CI.
- **Tests:** JUnit 5 + Mockito for unit tests. `@SpringBootTest` for integration tests. Minimum 70% coverage on business logic.
- **DTOs:** separate Request and Response DTOs per endpoint. Never expose domain entities directly.
- **Validation:** `@Valid` + Bean Validation on all request DTOs.
- **Error handling:** `@ControllerAdvice` with `ProblemDetail` (RFC 9457) for all error responses.

### React (frontend)

- **State:** Zustand for global state (user, active household). TanStack Query for server state.
- **Polling:** `useInterval` custom hook — active only when the component is mounted.
- **Optimistic UI:** update local state before server confirms; revert on error.
- **Components:** functional components + hooks only. No class components.
- **Style:** Tailwind CSS utility classes.

---

## Issue Labels

| Label | Meaning |
|---|---|
| `service:auth` | auth-service |
| `service:pantry` | pantry-service |
| `service:shopping` | shopping-service |
| `service:tasks` | tasks-service |
| `service:menu` | menu-service |
| `service:energy` | energy-service |
| `service:frontend` | frontend |
| `sprint:N` | Current sprint |
| `blocked` | Blocked by another issue |
| `tech-debt` | Technical debt |
| `bug` | Something is broken |
| `enhancement` | New feature or improvement |
