# HOEM — Product Requirements Document (PRD)

**Version:** 1.1
**Date:** 2026-05-12
**Status:** Approved — pre-MVP
**Reference standard:** ISO/IEC/IEEE 29148:2018 — Requirements engineering

---

## 1. Executive Summary

HOEM is a responsive web application for intelligent household management. It centralises five domains into a single connected system: pantry, shopping list, weekly menu, household tasks and energy optimisation.

Unlike generic list or task apps, HOEM connects these areas together: what is in the pantry determines suggested menus, menus determine the shopping list, the electricity price determines the best time to run appliances, and tasks are distributed among household members. The goal is to reduce the mental load of managing a home without the user losing control.

---

## 2. The Problem

Managing a household means making hundreds of microdecisions every week. Most people have no system for this and end up making reactive decisions, which leads to unnecessary spending, food waste and accumulated stress.

The root problem is not lack of willpower — it is lack of centralised information. Existing applications address each area separately but none of them connect them.

| Specific problem | User consequence |
|---|---|
| Disorganised pantry | Duplicate purchases, products expiring unused |
| No meal planning | Frequent takeaway orders, high spending |
| Unaware of electricity prices | Appliances used during expensive hours |
| Unassigned household tasks | Conflicts, forgotten chores, sense of unfairness |

---

## 3. Target Users

### Persona 1 — Laura, 32 (couple without organisation)

| Field | Detail |
|---|---|
| Situation | Lives with partner, both work full time |
| Main pain | Nobody remembers what is in the fridge. They order takeaway |
| Goal with HOEM | Share shopping list and menus with partner effortlessly |
| Most-used features | Shared list, weekly menus, tasks assigned per member |
| Representative quote | "I spend every Sunday thinking about what to cook. I wish someone would just figure it out for me." |

### Persona 2 — Miguel, 44 (father watching household costs)

| Field | Detail |
|---|---|
| Situation | Family of 4, manages household finances |
| Main pain | Electricity bill worries him but he does not know when to run appliances |
| Goal with HOEM | Know the best time to run washing machine and dishwasher; keep pantry under control |
| Most-used features | Real-time electricity price, cheap-rate alerts, family pantry |
| Representative quote | "I know I can save on electricity but I don't have time to check Red Eléctrica every day." |

### Persona 3 — Sara, 22 (student living alone for the first time)

| Field | Detail |
|---|---|
| Situation | Student flat, tight budget |
| Main pain | Throws food away because she doesn't track it; doesn't know what to cook |
| Goal with HOEM | Know what to cook with available ingredients; shop more efficiently |
| Most-used features | Recipes from available ingredients, low stock detection, smart shopping list |
| Representative quote | "I open the fridge and have no idea what to do. I end up ordering something and spending money I don't have." |

### Persona 4 — Claudia + Mateo (family with children, primary multi-member segment)

| Field | Detail |
|---|---|
| Situation | Couple with two children (5 and 8), shared domestic coordination |
| Main pain | Burden falls on one person. The other wants to help but has no visibility |
| Goal with HOEM | Real coordination between two adults with shared visibility |
| Most-used features | All modules, especially tasks and real-time shopping list |

---

## 4. Product Goals

### 4.1 Primary Goal

Enable anyone to manage their home more intelligently in less time, reducing spending and mental load, without requiring technical knowledge or special hardware integrations.

### 4.2 Specific v1 Goals

- A user can register and look up a pantry item in under 30 seconds per product.
- The automatically generated shopping list matches at least 70% of what the user actually buys.
- A user can plan their weekly menus without leaving the app, based on what they already have at home.
- A user can check the real-time electricity price and receive alerts for the cheapest rate of the day.
- Members of the same household can share pantry, lists and tasks in real time.

---

## 5. Functional Requirements

MoSCoW prioritisation: **Must** (mandatory MVP), **Should** (highly desirable), **Could** (if time permits), **Won't** (out of v1).

### 5.1 CORE Module — Authentication and Households

| ID | Requirement | Priority |
|---|---|---|
| FR-CORE-001 | Registration with email and password | MUST |
| FR-CORE-002 | Login with email and password | MUST |
| FR-CORE-003 | Password recovery via email | MUST |
| FR-CORE-004 | Create household; creator becomes admin | MUST |
| FR-CORE-005 | Invite members via 6-character code; admin must approve join requests | MUST |
| FR-CORE-006 | A user can belong to multiple households and switch between them | SHOULD |
| FR-CORE-007 | Roles: `admin` and `member` | MUST |
| FR-CORE-008 | Admin can remove members and change roles | MUST |
| FR-CORE-009 | Editable user profile (name, optional avatar) | SHOULD |

### 5.2 PANTRY Module

| ID | Requirement | Priority |
|---|---|---|
| FR-PANT-001 | Add products: name, category, quantity, unit, optional expiry date | MUST |
| FR-PANT-002 | Edit and delete products | MUST |
| FR-PANT-003 | Filterable and searchable product list | MUST |
| FR-PANT-004 | Grouping by category | SHOULD |
| FR-PANT-005 | Mark products below configurable threshold as "low stock" | MUST |
| FR-PANT-006 | Alerts for products expiring soon (≤ 3 days) | SHOULD |
| FR-PANT-007 | Decrement quantity with a single tap ("used one") | SHOULD |
| FR-PANT-008 | Consumption history per product | COULD |

### 5.3 SHOPPING Module

| ID | Requirement | Priority |
|---|---|---|
| FR-SHOP-001 | Create and manage multiple shopping lists per household | MUST |
| FR-SHOP-002 | Automatically suggest low-stock products | MUST |
| FR-SHOP-003 | Manually add any product to the list | MUST |
| FR-SHOP-004 | Mark items as purchased and optionally update pantry | MUST |
| FR-SHOP-005 | Shared list in real time between household members | MUST |
| FR-SHOP-006 | Suggest recurring items based on history | SHOULD |
| FR-SHOP-007 | Group items by category or aisle | COULD |

### 5.4 TASKS Module

| ID | Requirement | Priority |
|---|---|---|
| FR-TASK-001 | Create tasks with title, description, priority and due date | MUST |
| FR-TASK-002 | Recurring tasks (daily, weekly, monthly, custom) | MUST |
| FR-TASK-003 | Assign tasks to one or more members | MUST |
| FR-TASK-004 | Mark tasks as completed | MUST |
| FR-TASK-005 | Dashboard with pending tasks per member | MUST |
| FR-TASK-006 | Automatic fair distribution (round-robin in v1) | SHOULD |
| FR-TASK-007 | Email reminders for upcoming tasks | SHOULD |
| FR-TASK-008 | Distribution statistics per member | COULD |

### 5.5 MENU Module

| ID | Requirement | Priority |
|---|---|---|
| FR-MENU-001 | Save recipes with ingredients, quantities, steps, prep time, cook time, servings and category | MUST |
| FR-MENU-002 | Create weekly menu (lunch and dinner per day) | MUST |
| FR-MENU-003 | Assign recipes to plan slots | MUST |
| FR-MENU-004 | Suggest recipes based on current pantry contents (rule-based in v1, no ML) | SHOULD |
| FR-MENU-005 | Auto-generate shopping list with missing ingredients from plan | MUST |
| FR-MENU-006 | Create "cook X" tasks on the plan date | SHOULD |
| FR-MENU-007 | Global recipe catalogue shared across all households | MUST |
| FR-MENU-008 | Each household can create its own private recipes | MUST |

### 5.6 ENERGY Module

| ID | Requirement | Priority |
|---|---|---|
| FR-ENE-001 | Display current PVPC electricity price (€/kWh) | MUST |
| FR-ENE-002 | Classify each hour as cheap, medium or expensive | MUST |
| FR-ENE-003 | Show 24-hour price forecast with cheap/medium/expensive bands | MUST |
| FR-ENE-004 | Configurable cheap-rate alerts (email + in-app) | MUST |
| FR-ENE-005 | Appliance recommendations: best time slot + estimated saving | MUST |
| FR-ENE-006 | ESIOS fallback: show previous day data with warning if API is unavailable | MUST |
| FR-ENE-007 | Configure household appliances (name, wattage) | MUST |
| FR-ENE-008 | Estimated savings history | COULD |

---

## 6. Non-Functional Requirements

### 6.1 Performance

| ID | Requirement |
|---|---|
| NFR-PERF-001 | Initial load time < 2 seconds on 4G or WiFi |
| NFR-PERF-002 | P95 response time for read operations < 300 ms |
| NFR-PERF-003 | Shopping list synchronisation visible to observers in ≤ 5 seconds |
| NFR-PERF-004 | Support at least 50 concurrent users without degradation |

### 6.2 Security

| ID | Requirement |
|---|---|
| NFR-SEC-001 | Passwords stored with BCrypt (cost 12) |
| NFR-SEC-002 | HTTPS mandatory on all traffic (enforced by Cloudflare Tunnel) |
| NFR-SEC-003 | JWT access token expiry: 1 hour. Refresh token expiry: 30 days |
| NFR-SEC-004 | Sessions expire after 30 days of inactivity |
| NFR-SEC-005 | Secrets managed via Docker environment variables (never in code) |
| NFR-SEC-006 | Rate limiting on critical endpoints (auth): 10 req/min per IP |

### 6.3 Privacy and Compliance

| ID | Requirement |
|---|---|
| NFR-PRIV-001 | GDPR compliance: consent, access, rectification, erasure, portability |
| NFR-PRIV-002 | Public Privacy Policy and Terms of Use pages |
| NFR-PRIV-003 | User can download all their data in JSON |
| NFR-PRIV-004 | Permanent account deletion; personal data erased within ≤ 30 days |

### 6.4 Usability and Accessibility

| ID | Requirement |
|---|---|
| NFR-USA-001 | Interface meets WCAG 2.2 Level AA |
| NFR-USA-002 | Responsive from 320px (mobile) to 1920px (desktop) |
| NFR-USA-003 | Initial language: Spanish |
| NFR-USA-004 | Onboarding completable in ≤ 3 minutes |

### 6.5 Reliability

| ID | Requirement |
|---|---|
| NFR-REL-001 | Target availability: 99% monthly (accepts restarts from home server) |
| NFR-REL-002 | Automatic daily PostgreSQL backups with `pg_dump` to external drive |
| NFR-REL-003 | RPO: 24h. RTO: 2h (bring docker-compose back up) |

### 6.6 Maintainability

| ID | Requirement |
|---|---|
| NFR-MNT-001 | Minimum test coverage: 70% on business logic per service |
| NFR-MNT-002 | Conventional Commits 1.0 + SemVer 2.0 |
| NFR-MNT-003 | CI pipeline: build, tests, static analysis (Checkstyle + SpotBugs) |
| NFR-MNT-004 | Database migrations managed with Flyway per service |

---

## 7. Project Constraints

| Constraint | Detail |
|---|---|
| C-001 | Stack: Java 21 + Spring Boot 3.x + React 19 + PostgreSQL 16 |
| C-002 | Infrastructure: home server Windows 11 + WSL2 + Docker Desktop |
| C-003 | Internet exposure: Cloudflare Tunnel (free, no open ports) |
| C-004 | Budget: zero euros. Permanently free services only |
| C-005 | Team: single developer, part-time |
| C-006 | External APIs: free only. ESIOS is the only required external integration in v1 |
| C-007 | No ML in v1: suggestions are rule-based. Machine learning requires accumulated historical data |

---

## 8. Out of Scope — v1

| Out of scope | Reason |
|---|---|
| Appliance automation (switch on/off) | Requires paid hardware integrations |
| Automatic online grocery shopping | Paid integrations + legal risk |
| ML / neural networks for suggestions | Without historical data, models do not learn. Simple rules provide 80% of the value |
| Conversational AI chatbot | High API cost, unnecessary for v1 |
| Native mobile app (iOS/Android) | Responsive web covers v1; app goes in v2 |
| Payments and subscriptions module | Monetisation is not a v1 goal |
| Multi-language support | Spanish only in v1 |
| Receipt scanning with camera | Computer vision, high complexity for v1 |
| Supermarket integrations (Mercadona, Carrefour…) | Paid or unstable APIs |

---

## 9. Success Metrics

### 9.1 North Star Metric

> **Weekly Active Households (WAH)** that coordinate at least 3 actions between 2+ members in a week.

MVP goal (6 months): 30 WAH. Year 1 goal: 100 WAH.

### 9.2 Usage Metrics

| Metric | v1 Goal | How to measure |
|---|---|---|
| Register ≥ 1 product in first session | 70% of new users | DB event on first product created |
| Generate first list in first week | 60% of active users | Event on first list generated |
| Weekly usage frequency | ≥ 2 sessions/week per active user | Sessions per user |
| Users who invite another member | 30% of registered | Event on invite sent or accepted |
| Users who use energy module weekly | 40% of active users | Event on electricity price viewed |

### 9.3 Value Metrics

| Metric | v1 Goal | How to measure |
|---|---|---|
| Auto list match vs actual purchase | ≥ 70% of products match | Generated list vs checked as purchased |
| Users who change appliance schedule after alert | 40% of those who see the alert | In-app survey at 30 days |
| Declared reduction in food waste | 50% say they throw less food away | In-app survey at 60 days |
| NPS | > 30 | Survey at 14 and 30 days |

### 9.4 Technical Metrics

| Metric | Goal |
|---|---|
| Initial load time | < 2 seconds on 4G or WiFi |
| Member synchronisation | Changes visible in < 5 seconds |
| Monthly availability | ≥ 99% |
| Test coverage | ≥ 70% on critical services |

---

## 10. Design Decisions

| Decision | Discarded alternative | Reason |
|---|---|---|
| Rule-based suggestions in v1, no ML | LSTM / ML from the start | Without historical data models do not learn. Rules provide 80% of value at 5% of cost |
| Responsive web as first platform | Native app from the start | Halves development time. Mobile app in v2 once product is validated |
| Microservices with Docker from the start | Monolith first | Developer masters the stack; separating from the start avoids a costly rewrite later |
| Single PostgreSQL with schemas per service | One DB per microservice | Same logical isolation without the operational cost of 8 PostgreSQL instances |
| ESIOS API as sole energy integration | Supermarket APIs or private tariffs | ESIOS is free, stable, public and maintained by Red Eléctrica Española |
| No Redis in v1 | PostgreSQL + Redis from the start | YAGNI. Add it when there is a real measured need |
| Cloudflare Tunnel for exposure | VPS / Heroku / Railway | Permanently free, no open ports, automatic HTTPS, DDNS included |

---

## 11. Identified Risks

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Scope creep: wanting to add too much before finishing v1 | High | High | This PRD is the contract. Any new idea goes to the v2 backlog |
| ESIOS API changes or becomes unavailable | Low | Medium | Previous day data cache as fallback (FR-ENE-006) |
| Home server shuts down or fails | Medium | High | Automatic daily backups. Docker restart policy `unless-stopped` |
| Solo developer burnout | Medium | High | Short sprints, small milestones, celebrate each working delivery |
| Recipe database insufficient for menus | Medium | Medium | Start with 50–100 curated real recipes |
| User does not understand the value in the first session | Medium | High | Guided onboarding with pre-loaded household example |
| Insufficient RAM with all services running | Low | High | 13 GB available + JVM limit `-Xmx128m` per service. Monitor with `docker stats` |

---

## 12. Next Steps

| Step | What it is | When |
|---|---|---|
| C4 Architecture | Define services, ports, communication and docker-compose | Before writing code |
| Data model | PostgreSQL schema design per service with Flyway | Before writing code |
| Wireframes | Sketches of main screens including energy module | Before writing code |
| MVP — First working service | `auth-service` + `pantry-service` + basic frontend | Sprint 1 |
| ESIOS integration | `energy-service` connected to real API | Sprint 2 |
| PRD review | Update if anything important changes | Ongoing |

---

## Changelog

| Version | Date | Changes |
|---|---|---|
| 0.1 | 2026-05-10 | First draft (Next.js, no energy module) |
| 0.2 | 2026-05-10 | Second draft (Next.js, energy module out of scope) |
| 1.0 | 2026-05-10 | Unified version: Java/Spring Boot, microservices, Docker, energy module as P0 |
| 1.1 | 2026-05-12 | Translated to English. FR-CORE-005 updated to reflect 6-char invite code with admin approval flow. FR-MENU updated to include global + household recipes, numbered steps, prep/cook time, servings and category. FR-ENE-002 updated to reflect cheap/medium/expensive price bands |
