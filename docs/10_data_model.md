# HOEM — Modelo de datos

**Versión:** 1.0  
**Fecha:** 2026-05-12  
**Estado:** Aprobado — pre-MVP  
**Estándares aplicados:**
- PostgreSQL 16 — tipos nativos, índices parciales, arrays
- Flyway — migraciones versionadas por servicio (`V1__init.sql`)
- ADR-004 — una instancia PostgreSQL, schemas separados por servicio
- ADR-005 — Flyway por servicio, cada uno gestiona su propio schema

---

## Índice de schemas

| Schema | Servicio propietario | Tablas |
|---|---|---|
| `auth` | auth-service | users, households, memberships, invitations, join_requests, sessions, verification_tokens |
| `pantry` | pantry-service | categories, products, stock_alerts |
| `shopping` | shopping-service | lists, items, list_snapshots |
| `tasks` | tasks-service | tasks, recurrence_rules, assignments, comments |
| `menu` | menu-service | recipes, recipe_steps, recipe_ingredients, meal_plans, meal_slots, recipe_tags |
| `energy` | energy-service | price_cache, daily_stats, alerts, alert_triggers, appliances, savings_log |
| `notifications` | notification-service | templates, notification_log, in_app_notifications, preferences |

---

## Convenciones globales

- **PK:** `UUID` generado con `gen_random_uuid()`. Sin secuencias numéricas — los IDs se generan en la aplicación sin coordinación con la BD.
- **Timestamps:** `TIMESTAMPTZ` siempre. Almacena en UTC, convierte a la timezone del usuario en el frontend. Evita bugs de horario de verano.
- **Claves foráneas entre servicios:** no existen a nivel de BD. Cada servicio solo referencia tablas de su propio schema. La integridad entre servicios se garantiza a nivel de aplicación.
- **Soft delete:** no se usa en MVP. Los registros se eliminan físicamente. Se reevalúa en v2 si el producto lo requiere.
- **Nomenclatura:** snake_case en tablas y columnas. Nombres en inglés.

---

## Script de inicialización de schemas

Este script corre al arrancar PostgreSQL por primera vez (montado en `/docker-entrypoint-initdb.d/init.sql`):

```sql
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS pantry;
CREATE SCHEMA IF NOT EXISTS shopping;
CREATE SCHEMA IF NOT EXISTS tasks;
CREATE SCHEMA IF NOT EXISTS menu;
CREATE SCHEMA IF NOT EXISTS energy;
CREATE SCHEMA IF NOT EXISTS notifications;
```

---

## Schema `auth`

**Servicio:** `auth-service`  
**Migración:** `auth-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `auth.users`
```sql
CREATE TABLE auth.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    language      VARCHAR(10)  NOT NULL DEFAULT 'es',
    timezone      VARCHAR(50)  NOT NULL DEFAULT 'Europe/Madrid',
    verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON auth.users(email);
```

#### `auth.households`
```sql
CREATE TABLE auth.households (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NOT NULL, -- 'single','couple','family','flatmates'
    invite_code   CHAR(6)      NOT NULL UNIQUE DEFAULT upper(substring(gen_random_uuid()::text, 1, 6)),
    invite_active BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_households_invite_code ON auth.households(invite_code);
```

#### `auth.memberships`
```sql
CREATE TABLE auth.memberships (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    household_id UUID        NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    role         VARCHAR(20) NOT NULL DEFAULT 'member', -- 'admin','member'
    color        VARCHAR(7)  NOT NULL DEFAULT '#6366F1', -- color hex para tareas
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, household_id)
);

CREATE INDEX idx_memberships_user      ON auth.memberships(user_id);
CREATE INDEX idx_memberships_household ON auth.memberships(household_id);
```

#### `auth.invitations`
```sql
CREATE TABLE auth.invitations (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID         NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    invited_by   UUID         NOT NULL REFERENCES auth.users(id),
    email        VARCHAR(255) NOT NULL,
    token        VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ  NOT NULL,
    accepted_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_invitations_token ON auth.invitations(token);
CREATE INDEX idx_invitations_email ON auth.invitations(email);
```

#### `auth.join_requests`
```sql
CREATE TABLE auth.join_requests (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'pending', -- 'pending','accepted','rejected'
    reviewed_by  UUID        REFERENCES auth.users(id),
    reviewed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (household_id, user_id)
);

CREATE INDEX idx_join_requests_household ON auth.join_requests(household_id);
CREATE INDEX idx_join_requests_user      ON auth.join_requests(user_id);
```

#### `auth.sessions`
```sql
CREATE TABLE auth.sessions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_sessions_user          ON auth.sessions(user_id);
CREATE INDEX idx_sessions_refresh_token ON auth.sessions(refresh_token);
```

#### `auth.verification_tokens`
```sql
CREATE TABLE auth.verification_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_verification_tokens_token ON auth.verification_tokens(token);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE auth.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    language      VARCHAR(10)  NOT NULL DEFAULT 'es',
    timezone      VARCHAR(50)  NOT NULL DEFAULT 'Europe/Madrid',
    verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON auth.users(email);

CREATE TABLE auth.households (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    invite_code   CHAR(6)      NOT NULL UNIQUE DEFAULT upper(substring(gen_random_uuid()::text, 1, 6)),
    invite_active BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_households_invite_code ON auth.households(invite_code);

CREATE TABLE auth.memberships (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    household_id UUID        NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    role         VARCHAR(20) NOT NULL DEFAULT 'member',
    color        VARCHAR(7)  NOT NULL DEFAULT '#6366F1',
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, household_id)
);
CREATE INDEX idx_memberships_user      ON auth.memberships(user_id);
CREATE INDEX idx_memberships_household ON auth.memberships(household_id);

CREATE TABLE auth.invitations (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID         NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    invited_by   UUID         NOT NULL REFERENCES auth.users(id),
    email        VARCHAR(255) NOT NULL,
    token        VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ  NOT NULL,
    accepted_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_invitations_token ON auth.invitations(token);
CREATE INDEX idx_invitations_email ON auth.invitations(email);

CREATE TABLE auth.join_requests (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL REFERENCES auth.households(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'pending',
    reviewed_by  UUID        REFERENCES auth.users(id),
    reviewed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (household_id, user_id)
);
CREATE INDEX idx_join_requests_household ON auth.join_requests(household_id);
CREATE INDEX idx_join_requests_user      ON auth.join_requests(user_id);

CREATE TABLE auth.sessions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_user          ON auth.sessions(user_id);
CREATE INDEX idx_sessions_refresh_token ON auth.sessions(refresh_token);

CREATE TABLE auth.verification_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_verification_tokens_token ON auth.verification_tokens(token);
```

---

## Schema `pantry`

**Servicio:** `pantry-service`  
**Migración:** `pantry-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `pantry.categories`
```sql
CREATE TABLE pantry.categories (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL,
    icon         VARCHAR(50),
    is_global    BOOLEAN      NOT NULL DEFAULT FALSE,
    household_id UUID,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_categories_household ON pantry.categories(household_id);
```

#### `pantry.products`
```sql
CREATE TABLE pantry.products (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id   UUID          NOT NULL,
    category_id    UUID          REFERENCES pantry.categories(id) ON DELETE SET NULL,
    name           VARCHAR(200)  NOT NULL,
    brand          VARCHAR(100),
    quantity       NUMERIC(10,2) NOT NULL DEFAULT 0,
    unit           VARCHAR(20)   NOT NULL, -- 'kg','g','l','ml','units'
    min_quantity   NUMERIC(10,2),
    expiry_date    DATE,
    location       VARCHAR(50),            -- 'fridge','freezer','pantry'
    barcode        VARCHAR(100),
    notes          TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_household ON pantry.products(household_id);
CREATE INDEX idx_products_category  ON pantry.products(category_id);
CREATE INDEX idx_products_expiry    ON pantry.products(expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX idx_products_barcode   ON pantry.products(barcode) WHERE barcode IS NOT NULL;
```

#### `pantry.stock_alerts`
```sql
CREATE TABLE pantry.stock_alerts (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id   UUID        NOT NULL REFERENCES pantry.products(id) ON DELETE CASCADE,
    household_id UUID        NOT NULL,
    type         VARCHAR(20) NOT NULL, -- 'low_stock','expiring_soon','expired'
    resolved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_alerts_household ON pantry.stock_alerts(household_id);
CREATE INDEX idx_stock_alerts_product   ON pantry.stock_alerts(product_id);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE pantry.categories (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL,
    icon         VARCHAR(50),
    is_global    BOOLEAN      NOT NULL DEFAULT FALSE,
    household_id UUID,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_categories_household ON pantry.categories(household_id);

CREATE TABLE pantry.products (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id   UUID          NOT NULL,
    category_id    UUID          REFERENCES pantry.categories(id) ON DELETE SET NULL,
    name           VARCHAR(200)  NOT NULL,
    brand          VARCHAR(100),
    quantity       NUMERIC(10,2) NOT NULL DEFAULT 0,
    unit           VARCHAR(20)   NOT NULL,
    min_quantity   NUMERIC(10,2),
    expiry_date    DATE,
    location       VARCHAR(50),
    barcode        VARCHAR(100),
    notes          TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_household ON pantry.products(household_id);
CREATE INDEX idx_products_category  ON pantry.products(category_id);
CREATE INDEX idx_products_expiry    ON pantry.products(expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX idx_products_barcode   ON pantry.products(barcode) WHERE barcode IS NOT NULL;

CREATE TABLE pantry.stock_alerts (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id   UUID        NOT NULL REFERENCES pantry.products(id) ON DELETE CASCADE,
    household_id UUID        NOT NULL,
    type         VARCHAR(20) NOT NULL,
    resolved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_stock_alerts_household ON pantry.stock_alerts(household_id);
CREATE INDEX idx_stock_alerts_product   ON pantry.stock_alerts(product_id);
```

---

## Schema `shopping`

**Servicio:** `shopping-service`  
**Migración:** `shopping-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `shopping.lists`
```sql
CREATE TABLE shopping.lists (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID         NOT NULL,
    name         VARCHAR(200) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'active', -- 'active','completed','archived'
    created_by   UUID         NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_lists_household ON shopping.lists(household_id);
CREATE INDEX idx_lists_status    ON shopping.lists(household_id, status);
```

#### `shopping.items`
```sql
CREATE TABLE shopping.items (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id      UUID          NOT NULL REFERENCES shopping.lists(id) ON DELETE CASCADE,
    name         VARCHAR(200)  NOT NULL,
    quantity     NUMERIC(10,2) NOT NULL DEFAULT 1,
    unit         VARCHAR(20),
    category     VARCHAR(100),
    notes        TEXT,
    is_checked   BOOLEAN       NOT NULL DEFAULT FALSE,
    checked_by   UUID,
    checked_at   TIMESTAMPTZ,
    sort_order   INTEGER       NOT NULL DEFAULT 0,
    created_by   UUID          NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_items_list       ON shopping.items(list_id);
CREATE INDEX idx_items_is_checked ON shopping.items(list_id, is_checked);
```

#### `shopping.list_snapshots`
```sql
CREATE TABLE shopping.list_snapshots (
    list_id       UUID        NOT NULL REFERENCES shopping.lists(id) ON DELETE CASCADE,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    items_total   INTEGER     NOT NULL DEFAULT 0,
    items_checked INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (list_id)
);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE shopping.lists (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID         NOT NULL,
    name         VARCHAR(200) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_by   UUID         NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_lists_household ON shopping.lists(household_id);
CREATE INDEX idx_lists_status    ON shopping.lists(household_id, status);

CREATE TABLE shopping.items (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id      UUID          NOT NULL REFERENCES shopping.lists(id) ON DELETE CASCADE,
    name         VARCHAR(200)  NOT NULL,
    quantity     NUMERIC(10,2) NOT NULL DEFAULT 1,
    unit         VARCHAR(20),
    category     VARCHAR(100),
    notes        TEXT,
    is_checked   BOOLEAN       NOT NULL DEFAULT FALSE,
    checked_by   UUID,
    checked_at   TIMESTAMPTZ,
    sort_order   INTEGER       NOT NULL DEFAULT 0,
    created_by   UUID          NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_items_list       ON shopping.items(list_id);
CREATE INDEX idx_items_is_checked ON shopping.items(list_id, is_checked);

CREATE TABLE shopping.list_snapshots (
    list_id       UUID        NOT NULL REFERENCES shopping.lists(id) ON DELETE CASCADE,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    items_total   INTEGER     NOT NULL DEFAULT 0,
    items_checked INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (list_id)
);
```

---

## Schema `tasks`

**Servicio:** `tasks-service`  
**Migración:** `tasks-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `tasks.recurrence_rules`
```sql
CREATE TABLE tasks.recurrence_rules (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID        NOT NULL,
    frequency     VARCHAR(20) NOT NULL, -- 'daily','weekly','monthly','yearly'
    interval      INTEGER     NOT NULL DEFAULT 1,
    days_of_week  INTEGER[],            -- [1,3,5] = lunes, miércoles, viernes
    day_of_month  INTEGER,
    ends_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_recurrence_household ON tasks.recurrence_rules(household_id);
```

#### `tasks.tasks`
```sql
CREATE TABLE tasks.tasks (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID         NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'pending', -- 'pending','in_progress','done','cancelled'
    priority      VARCHAR(20)  NOT NULL DEFAULT 'medium',  -- 'low','medium','high'
    due_date      TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    completed_by  UUID,
    is_recurring  BOOLEAN      NOT NULL DEFAULT FALSE,
    recurrence_id UUID         REFERENCES tasks.recurrence_rules(id) ON DELETE SET NULL,
    created_by    UUID         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_household ON tasks.tasks(household_id);
CREATE INDEX idx_tasks_due_date  ON tasks.tasks(household_id, due_date);
CREATE INDEX idx_tasks_status    ON tasks.tasks(household_id, status);
```

#### `tasks.assignments`
```sql
CREATE TABLE tasks.assignments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID        NOT NULL REFERENCES tasks.tasks(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL,
    assigned_by UUID        NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (task_id, user_id)
);

CREATE INDEX idx_assignments_task ON tasks.assignments(task_id);
CREATE INDEX idx_assignments_user ON tasks.assignments(user_id);
```

#### `tasks.comments`
```sql
CREATE TABLE tasks.comments (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID        NOT NULL REFERENCES tasks.tasks(id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_task ON tasks.comments(task_id);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE tasks.recurrence_rules (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL,
    frequency    VARCHAR(20) NOT NULL,
    interval     INTEGER     NOT NULL DEFAULT 1,
    days_of_week INTEGER[],
    day_of_month INTEGER,
    ends_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recurrence_household ON tasks.recurrence_rules(household_id);

CREATE TABLE tasks.tasks (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID         NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'pending',
    priority      VARCHAR(20)  NOT NULL DEFAULT 'medium',
    due_date      TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    completed_by  UUID,
    is_recurring  BOOLEAN      NOT NULL DEFAULT FALSE,
    recurrence_id UUID         REFERENCES tasks.recurrence_rules(id) ON DELETE SET NULL,
    created_by    UUID         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_tasks_household ON tasks.tasks(household_id);
CREATE INDEX idx_tasks_due_date  ON tasks.tasks(household_id, due_date);
CREATE INDEX idx_tasks_status    ON tasks.tasks(household_id, status);

CREATE TABLE tasks.assignments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID        NOT NULL REFERENCES tasks.tasks(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL,
    assigned_by UUID        NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (task_id, user_id)
);
CREATE INDEX idx_assignments_task ON tasks.assignments(task_id);
CREATE INDEX idx_assignments_user ON tasks.assignments(user_id);

CREATE TABLE tasks.comments (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID        NOT NULL REFERENCES tasks.tasks(id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_comments_task ON tasks.comments(task_id);
```

---

## Schema `menu`

**Servicio:** `menu-service`  
**Migración:** `menu-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `menu.recipes`
```sql
CREATE TABLE menu.recipes (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID,                     -- NULL = receta global del sistema
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    category      VARCHAR(50)  NOT NULL,    -- 'breakfast','lunch','dinner','snack','dessert'
    difficulty    VARCHAR(20)  NOT NULL DEFAULT 'medium', -- 'easy','medium','hard'
    prep_time_min INTEGER,
    cook_time_min INTEGER,
    servings      INTEGER      NOT NULL DEFAULT 2,
    image_url     VARCHAR(500),
    source        VARCHAR(500),
    is_global     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by    UUID,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_recipes_household ON menu.recipes(household_id);
CREATE INDEX idx_recipes_category  ON menu.recipes(category);
CREATE INDEX idx_recipes_global    ON menu.recipes(is_global) WHERE is_global = TRUE;
```

#### `menu.recipe_steps`
```sql
CREATE TABLE menu.recipe_steps (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id    UUID        NOT NULL REFERENCES menu.recipes(id) ON DELETE CASCADE,
    step_number  INTEGER     NOT NULL,
    instruction  TEXT        NOT NULL,
    duration_min INTEGER,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (recipe_id, step_number)
);

CREATE INDEX idx_recipe_steps_recipe ON menu.recipe_steps(recipe_id);
```

#### `menu.recipe_ingredients`
```sql
CREATE TABLE menu.recipe_ingredients (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id   UUID          NOT NULL REFERENCES menu.recipes(id) ON DELETE CASCADE,
    name        VARCHAR(200)  NOT NULL,
    quantity    NUMERIC(10,2) NOT NULL,
    unit        VARCHAR(20)   NOT NULL, -- 'kg','g','l','ml','units','tbsp','tsp'
    is_optional BOOLEAN       NOT NULL DEFAULT FALSE,
    notes       VARCHAR(200),
    sort_order  INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_recipe_ingredients_recipe ON menu.recipe_ingredients(recipe_id);
```

#### `menu.meal_plans`
```sql
CREATE TABLE menu.meal_plans (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL,
    week_start   DATE        NOT NULL, -- siempre lunes
    created_by   UUID        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (household_id, week_start)
);

CREATE INDEX idx_meal_plans_household ON menu.meal_plans(household_id);
```

#### `menu.meal_slots`
```sql
CREATE TABLE menu.meal_slots (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id     UUID        NOT NULL REFERENCES menu.meal_plans(id) ON DELETE CASCADE,
    recipe_id   UUID        REFERENCES menu.recipes(id) ON DELETE SET NULL,
    day_of_week INTEGER     NOT NULL, -- 1=lunes ... 7=domingo
    meal_type   VARCHAR(20) NOT NULL, -- 'breakfast','lunch','dinner','snack'
    servings    INTEGER     NOT NULL DEFAULT 2,
    notes       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (plan_id, day_of_week, meal_type)
);

CREATE INDEX idx_meal_slots_plan ON menu.meal_slots(plan_id);
```

#### `menu.recipe_tags`
```sql
CREATE TABLE menu.recipe_tags (
    recipe_id UUID        NOT NULL REFERENCES menu.recipes(id) ON DELETE CASCADE,
    tag       VARCHAR(50) NOT NULL,
    PRIMARY KEY (recipe_id, tag)
);

CREATE INDEX idx_recipe_tags_tag ON menu.recipe_tags(tag);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE menu.recipes (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    category      VARCHAR(50)  NOT NULL,
    difficulty    VARCHAR(20)  NOT NULL DEFAULT 'medium',
    prep_time_min INTEGER,
    cook_time_min INTEGER,
    servings      INTEGER      NOT NULL DEFAULT 2,
    image_url     VARCHAR(500),
    source        VARCHAR(500),
    is_global     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by    UUID,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_recipes_household ON menu.recipes(household_id);
CREATE INDEX idx_recipes_category  ON menu.recipes(category);
CREATE INDEX idx_recipes_global    ON menu.recipes(is_global) WHERE is_global = TRUE;

CREATE TABLE menu.recipe_steps (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id    UUID        NOT NULL REFERENCES menu.recipes(id) ON DELETE CASCADE,
    step_number  INTEGER     NOT NULL,
    instruction  TEXT        NOT NULL,
    duration_min INTEGER,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (recipe_id, step_number)
);
CREATE INDEX idx_recipe_steps_recipe ON menu.recipe_steps(recipe_id);

CREATE TABLE menu.recipe_ingredients (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id   UUID          NOT NULL REFERENCES menu.recipes(id) ON DELETE CASCADE,
    name        VARCHAR(200)  NOT NULL,
    quantity    NUMERIC(10,2) NOT NULL,
    unit        VARCHAR(20)   NOT NULL,
    is_optional BOOLEAN       NOT NULL DEFAULT FALSE,
    notes       VARCHAR(200),
    sort_order  INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_recipe_ingredients_recipe ON menu.recipe_ingredients(recipe_id);

CREATE TABLE menu.meal_plans (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL,
    week_start   DATE        NOT NULL,
    created_by   UUID        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (household_id, week_start)
);
CREATE INDEX idx_meal_plans_household ON menu.meal_plans(household_id);

CREATE TABLE menu.meal_slots (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id     UUID        NOT NULL REFERENCES menu.meal_plans(id) ON DELETE CASCADE,
    recipe_id   UUID        REFERENCES menu.recipes(id) ON DELETE SET NULL,
    day_of_week INTEGER     NOT NULL,
    meal_type   VARCHAR(20) NOT NULL,
    servings    INTEGER     NOT NULL DEFAULT 2,
    notes       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (plan_id, day_of_week, meal_type)
);
CREATE INDEX idx_meal_slots_plan ON menu.meal_slots(plan_id);

CREATE TABLE menu.recipe_tags (
    recipe_id UUID        NOT NULL REFERENCES menu.recipes(id) ON DELETE CASCADE,
    tag       VARCHAR(50) NOT NULL,
    PRIMARY KEY (recipe_id, tag)
);
CREATE INDEX idx_recipe_tags_tag ON menu.recipe_tags(tag);
```

---

## Schema `energy`

**Servicio:** `energy-service`  
**Migración:** `energy-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `energy.price_cache`
```sql
CREATE TABLE energy.price_cache (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    hour       TIMESTAMPTZ   NOT NULL UNIQUE,
    price_kwh  NUMERIC(10,6) NOT NULL,
    price_band VARCHAR(10)   NOT NULL, -- 'cheap','medium','expensive'
    fetched_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_price_cache_hour ON energy.price_cache(hour);
```

#### `energy.daily_stats`
```sql
CREATE TABLE energy.daily_stats (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    date            DATE          NOT NULL UNIQUE,
    avg_price       NUMERIC(10,6) NOT NULL,
    min_price       NUMERIC(10,6) NOT NULL,
    max_price       NUMERIC(10,6) NOT NULL,
    cheap_hours     INTEGER[]     NOT NULL, -- horas con price_band = 'cheap'
    medium_hours    INTEGER[]     NOT NULL, -- horas con price_band = 'medium'
    expensive_hours INTEGER[]     NOT NULL, -- horas con price_band = 'expensive'
    fetched_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_daily_stats_date ON energy.daily_stats(date);
```

#### `energy.alerts`
```sql
CREATE TABLE energy.alerts (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID          NOT NULL,
    threshold_kwh NUMERIC(10,6) NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    notify_email  BOOLEAN       NOT NULL DEFAULT TRUE,
    notify_in_app BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by    UUID          NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_alerts_household ON energy.alerts(household_id);
CREATE INDEX idx_alerts_active    ON energy.alerts(is_active) WHERE is_active = TRUE;
```

#### `energy.alert_triggers`
```sql
CREATE TABLE energy.alert_triggers (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id     UUID          NOT NULL REFERENCES energy.alerts(id) ON DELETE CASCADE,
    household_id UUID          NOT NULL,
    triggered_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    price_kwh    NUMERIC(10,6) NOT NULL,
    notified_at  TIMESTAMPTZ
);

CREATE INDEX idx_alert_triggers_alert     ON energy.alert_triggers(alert_id);
CREATE INDEX idx_alert_triggers_household ON energy.alert_triggers(household_id);
```

#### `energy.appliances`
```sql
CREATE TABLE energy.appliances (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id     UUID         NOT NULL,
    name             VARCHAR(200) NOT NULL,
    power_watts      INTEGER      NOT NULL,
    avg_duration_min INTEGER      NOT NULL,
    is_flexible      BOOLEAN      NOT NULL DEFAULT TRUE,
    icon             VARCHAR(50),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_appliances_household ON energy.appliances(household_id);
```

#### `energy.savings_log`
```sql
CREATE TABLE energy.savings_log (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID          NOT NULL,
    appliance_id  UUID          REFERENCES energy.appliances(id) ON DELETE SET NULL,
    used_at       TIMESTAMPTZ   NOT NULL,
    duration_min  INTEGER       NOT NULL,
    price_kwh     NUMERIC(10,6) NOT NULL,
    avg_price_kwh NUMERIC(10,6) NOT NULL,
    saving_eur    NUMERIC(10,4) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_savings_log_household ON energy.savings_log(household_id);
CREATE INDEX idx_savings_log_used_at   ON energy.savings_log(household_id, used_at);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE energy.price_cache (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    hour       TIMESTAMPTZ   NOT NULL UNIQUE,
    price_kwh  NUMERIC(10,6) NOT NULL,
    price_band VARCHAR(10)   NOT NULL,
    fetched_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_price_cache_hour ON energy.price_cache(hour);

CREATE TABLE energy.daily_stats (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    date            DATE          NOT NULL UNIQUE,
    avg_price       NUMERIC(10,6) NOT NULL,
    min_price       NUMERIC(10,6) NOT NULL,
    max_price       NUMERIC(10,6) NOT NULL,
    cheap_hours     INTEGER[]     NOT NULL,
    medium_hours    INTEGER[]     NOT NULL,
    expensive_hours INTEGER[]     NOT NULL,
    fetched_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_daily_stats_date ON energy.daily_stats(date);

CREATE TABLE energy.alerts (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID          NOT NULL,
    threshold_kwh NUMERIC(10,6) NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    notify_email  BOOLEAN       NOT NULL DEFAULT TRUE,
    notify_in_app BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by    UUID          NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_alerts_household ON energy.alerts(household_id);
CREATE INDEX idx_alerts_active    ON energy.alerts(is_active) WHERE is_active = TRUE;

CREATE TABLE energy.alert_triggers (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id     UUID          NOT NULL REFERENCES energy.alerts(id) ON DELETE CASCADE,
    household_id UUID          NOT NULL,
    triggered_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    price_kwh    NUMERIC(10,6) NOT NULL,
    notified_at  TIMESTAMPTZ
);
CREATE INDEX idx_alert_triggers_alert     ON energy.alert_triggers(alert_id);
CREATE INDEX idx_alert_triggers_household ON energy.alert_triggers(household_id);

CREATE TABLE energy.appliances (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id     UUID         NOT NULL,
    name             VARCHAR(200) NOT NULL,
    power_watts      INTEGER      NOT NULL,
    avg_duration_min INTEGER      NOT NULL,
    is_flexible      BOOLEAN      NOT NULL DEFAULT TRUE,
    icon             VARCHAR(50),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_appliances_household ON energy.appliances(household_id);

CREATE TABLE energy.savings_log (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID          NOT NULL,
    appliance_id  UUID          REFERENCES energy.appliances(id) ON DELETE SET NULL,
    used_at       TIMESTAMPTZ   NOT NULL,
    duration_min  INTEGER       NOT NULL,
    price_kwh     NUMERIC(10,6) NOT NULL,
    avg_price_kwh NUMERIC(10,6) NOT NULL,
    saving_eur    NUMERIC(10,4) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_savings_log_household ON energy.savings_log(household_id);
CREATE INDEX idx_savings_log_used_at   ON energy.savings_log(household_id, used_at);
```

---

## Schema `notifications`

**Servicio:** `notification-service`  
**Migración:** `notification-service/src/main/resources/db/migration/V1__init.sql`

### Tablas

#### `notifications.templates`
```sql
CREATE TABLE notifications.templates (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(50)  NOT NULL UNIQUE, -- 'email_verification','invite_member','energy_alert'
    channel    VARCHAR(20)  NOT NULL,        -- 'email','in_app'
    subject    VARCHAR(200),
    body       TEXT         NOT NULL,        -- plantilla con variables {{name}}, {{household}}
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_templates_code ON notifications.templates(code);
```

#### `notifications.notification_log`
```sql
CREATE TABLE notifications.notification_log (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID,
    user_id       UUID        NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    channel       VARCHAR(20) NOT NULL,    -- 'email','in_app'
    status        VARCHAR(20) NOT NULL DEFAULT 'pending', -- 'pending','sent','failed'
    payload       JSONB,
    sent_at       TIMESTAMPTZ,
    failed_at     TIMESTAMPTZ,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_log_user      ON notifications.notification_log(user_id);
CREATE INDEX idx_notification_log_household ON notifications.notification_log(household_id);
CREATE INDEX idx_notification_log_status    ON notifications.notification_log(status) WHERE status = 'pending';
```

#### `notifications.in_app_notifications`
```sql
CREATE TABLE notifications.in_app_notifications (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    household_id UUID,
    type         VARCHAR(50)  NOT NULL, -- 'energy_alert','low_stock','task_assigned','invite_received'
    title        VARCHAR(200) NOT NULL,
    body         TEXT,
    action_url   VARCHAR(500),
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_in_app_user      ON notifications.in_app_notifications(user_id);
CREATE INDEX idx_in_app_unread    ON notifications.in_app_notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_in_app_household ON notifications.in_app_notifications(household_id);
```

#### `notifications.preferences`
```sql
CREATE TABLE notifications.preferences (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id         UUID        NOT NULL UNIQUE,
    energy_alerts_email  BOOLEAN     NOT NULL DEFAULT TRUE,
    energy_alerts_in_app BOOLEAN     NOT NULL DEFAULT TRUE,
    low_stock_email      BOOLEAN     NOT NULL DEFAULT FALSE,
    low_stock_in_app     BOOLEAN     NOT NULL DEFAULT TRUE,
    task_assigned_email  BOOLEAN     NOT NULL DEFAULT FALSE,
    task_assigned_in_app BOOLEAN     NOT NULL DEFAULT TRUE,
    expiry_email         BOOLEAN     NOT NULL DEFAULT FALSE,
    expiry_in_app        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_preferences_household ON notifications.preferences(household_id);
```

### Migración Flyway — `V1__init.sql`

```sql
CREATE TABLE notifications.templates (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(50)  NOT NULL UNIQUE,
    channel    VARCHAR(20)  NOT NULL,
    subject    VARCHAR(200),
    body       TEXT         NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_templates_code ON notifications.templates(code);

CREATE TABLE notifications.notification_log (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id  UUID,
    user_id       UUID        NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    channel       VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'pending',
    payload       JSONB,
    sent_at       TIMESTAMPTZ,
    failed_at     TIMESTAMPTZ,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_log_user      ON notifications.notification_log(user_id);
CREATE INDEX idx_notification_log_household ON notifications.notification_log(household_id);
CREATE INDEX idx_notification_log_status    ON notifications.notification_log(status) WHERE status = 'pending';

CREATE TABLE notifications.in_app_notifications (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    household_id UUID,
    type         VARCHAR(50)  NOT NULL,
    title        VARCHAR(200) NOT NULL,
    body         TEXT,
    action_url   VARCHAR(500),
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_in_app_user      ON notifications.in_app_notifications(user_id);
CREATE INDEX idx_in_app_unread    ON notifications.in_app_notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_in_app_household ON notifications.in_app_notifications(household_id);

CREATE TABLE notifications.preferences (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id         UUID        NOT NULL UNIQUE,
    energy_alerts_email  BOOLEAN     NOT NULL DEFAULT TRUE,
    energy_alerts_in_app BOOLEAN     NOT NULL DEFAULT TRUE,
    low_stock_email      BOOLEAN     NOT NULL DEFAULT FALSE,
    low_stock_in_app     BOOLEAN     NOT NULL DEFAULT TRUE,
    task_assigned_email  BOOLEAN     NOT NULL DEFAULT FALSE,
    task_assigned_in_app BOOLEAN     NOT NULL DEFAULT TRUE,
    expiry_email         BOOLEAN     NOT NULL DEFAULT FALSE,
    expiry_in_app        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_preferences_household ON notifications.preferences(household_id);
```

---

## Resumen de tablas por schema

| Schema | Tablas | Índices |
|---|---|---|
| `auth` | 7 | 10 |
| `pantry` | 3 | 5 |
| `shopping` | 3 | 3 |
| `tasks` | 4 | 7 |
| `menu` | 6 | 8 |
| `energy` | 6 | 8 |
| `notifications` | 4 | 6 |
| **Total** | **33** | **47** |

---

## Historial de cambios

| Versión | Fecha | Cambios |
|---|---|---|
| 1.0 | 2026-05-12 | Versión inicial — 7 schemas, 33 tablas, 47 índices, migraciones Flyway V1 incluidas |
