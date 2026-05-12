# HOEM — Success Metrics

**Version:** 1.1
**Date:** 2026-05-12
**Frameworks applied:**
- **HEART** (Google Research, 2010) — user experience-centred metrics
- **AARRR / Pirate Metrics** (Dave McClure, 500 Startups) — product funnel metrics
- **OKR** (Andy Grove, Intel; popularised by Google) — for MVP objectives

---

## 1. North Star Metric

> **Weekly Active Households (WAH)** that coordinate at least **3 actions** between 2+ members in a week.

**Rationale:** if a household coordinates several actions between several members in a week, HOEM is fulfilling its mission of reducing the mental load on a single person. If not, the product is failing.

**MVP goal (6 months after beta launch):** 30 WAH.
**Year 1 goal:** 100 WAH.

---

## 2. HEART Framework

| Category | Metric | How to measure | MVP goal |
|---|---|---|---|
| **Happiness** | NPS (Net Promoter Score) | In-app survey at 14 and 30 days | NPS > 30 |
| **Happiness** | Module CSAT (1–5) | Survey after using each module | ≥ 4.0 / 5 |
| **Engagement** | Actions per active user / week | PostHog events | > 8 |
| **Engagement** | DAU/WAU ratio | PostHog | > 0.4 |
| **Engagement** | Users who check energy module weekly | PostHog events | > 40% of active users |
| **Adoption** | % users who invite ≥ 1 member | DB data | > 40% |
| **Adoption** | % users who use ≥ 3 modules | Events | > 60% |
| **Adoption** | % users who activate energy alerts | Events | > 50% |
| **Retention** | Week 4 retention | PostHog cohorts | > 35% |
| **Retention** | Monthly churn | DB + analytics | < 10% monthly |
| **Task success** | Onboarding completion rate | Events | > 75% |
| **Task success** | Avg time to create first task/list | Events | < 90 seconds |
| **Task success** | % users who change appliance schedule after alert | 30-day survey | > 40% |

---

## 3. AARRR Funnel (Pirate Metrics)

### Acquisition

| Metric | MVP goal |
|---|---|
| Unique visits/month to landing page | 500 |
| Visit → registration conversion rate | > 3% |
| Acquisition cost (CAC) | €0 in MVP (organic channels: friends, social, forums) |

### Activation

| Metric | MVP goal |
|---|---|
| % users who complete onboarding | > 75% |
| % users who create their first household | > 90% |
| % users who add ≥ 5 products to pantry within 24h | > 50% |
| % users who invite at least 1 member | > 40% |
| % users who check the energy module in the first 48h | > 60% |

### Retention

| Metric | MVP goal |
|---|---|
| Day 1 retention | > 60% |
| Day 7 retention | > 40% |
| Day 30 retention | > 25% |

### Referral

| Metric | MVP goal |
|---|---|
| K-factor (invitations × acceptance rate) | > 0.5 |
| Households with ≥ 2 active members | > 60% of total |

### Revenue

> For the MVP, **revenue = 0**. Monetisation is validated in a later phase once retention is confirmed.

---

## 4. MVP OKRs

### Objective 1: Validate that HOEM reduces perceived mental load

| Key Result | Baseline | Q3 2026 Goal |
|---|---|---|
| KR1.1 — NPS from beta users | — | ≥ 30 |
| KR1.2 — Self-reported mental load reduction after 4 weeks | — | ≥ 30% (validated survey) |
| KR1.3 — % users who say "I would not coordinate home without HOEM" | — | ≥ 50% |

### Objective 2: Demonstrate multi-member engagement

| Key Result | Baseline | Q3 2026 Goal |
|---|---|---|
| KR2.1 — Households with ≥ 2 active members weekly | 0 | 30 |
| KR2.2 — % tasks completed by members other than admin | — | ≥ 40% |
| KR2.3 — Collaborative shopping lists (edited by ≥ 2 people) per week | 0 | 50 |

### Objective 3: Demonstrate value of the energy module

| Key Result | Baseline | Q3 2026 Goal |
|---|---|---|
| KR3.1 — % active users who use the energy module weekly | 0% | ≥ 40% |
| KR3.2 — % users who report changing appliance schedule thanks to HOEM | — | ≥ 30% |
| KR3.3 — Average estimated saving per active household per month | — | ≥ €5/month |

### Objective 4: Build a technically solid product

| Key Result | Baseline | Q3 2026 Goal |
|---|---|---|
| KR4.1 — Test coverage per service | 0% | ≥ 70% |
| KR4.2 — Critical bugs in production | — | < 2 per month |
| KR4.3 — P95 response time for reads | — | < 300 ms |
| KR4.4 — Monthly uptime | — | ≥ 99% |
| KR4.5 — All services running with a single command | No | `docker-compose up -d` ✓ |

---

## 5. Anti-Metrics (what NOT to track)

-  **Total registrations** without activity context.
-  **Average time in app** (more time is not necessarily better; HOEM should save time, not consume it).
-  **Total products in pantry** accumulated (a user can fill the pantry once and abandon).
-  **Social media likes** without qualified traffic to the app.
-  **Number of energy alerts sent** without measuring whether the user acted on them.

---

## 6. Instrumentation Plan

Tool: **PostHog** (open source, free tier 1M events/month).

Each Spring Boot service records events via a PostHog Java client. `userId` and `householdId` travel in all events to enable cohort building.

### Critical events to track

```
# Auth
user_signed_up           { method: "email" }
household_created        { type, member_count }
member_invited           { household_id }
member_joined            { household_id }

# Pantry
pantry_item_added        { category, has_expiry }
pantry_item_decremented  { reached_zero: bool }
pantry_low_stock_alert   { product_id }

# Shopping
shopping_list_created    { source: "manual" | "auto" | "from_plan" }
shopping_item_purchased  { updated_pantry: bool }
shopping_list_completed  { item_count, duration_minutes }

# Tasks
task_created             { recurring: bool, assigned_count }
task_completed           { assignee_id, on_time: bool }
task_auto_assigned       { algorithm: "round_robin" }

# Menu
recipe_created           { ingredient_count, step_count }
meal_plan_slot_filled    { source: "manual" | "suggestion" }
shopping_list_from_plan  { missing_items_count }

# Energy
energy_module_viewed     { }
energy_alert_triggered   { price_kwh, hour, band: "cheap" }
energy_alert_configured  { threshold_kwh }
appliance_added          { power_watts }
energy_recommendation_viewed { best_hour, estimated_saving }
```

> **Naming convention:** event in `snake_case`, in simple past tense (`item_added`, not `add_item`). Consistency from day one avoids later refactors.

---

## 7. Review Cadence

| Frequency | What to review |
|---|---|
| **Daily** | Error logs per Docker service, server uptime |
| **Weekly** | DAU/WAU, weekly retention, energy module usage, new bugs |
| **Monthly** | Retention cohorts, NPS, OKRs, estimated saving per household |
| **Quarterly** | Full OKR review, goal adjustment, new feature decisions |

---

## Changelog

| Version | Date | Changes |
|---|---|---|
| 0.1 | 2026-05-10 | Initial version (no energy module) |
| 1.0 | 2026-05-10 | Objective 3 (energy) added to OKRs. HEART metrics expanded with energy. Energy events added to instrumentation plan |
| 1.1 | 2026-05-12 | Translated to English. Energy events updated to reflect cheap/medium/expensive price bands |
