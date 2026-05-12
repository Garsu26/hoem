# HOEM — Vision Document

**Version:** 1.1
**Date:** 2026-05-12
**Status:** Approved — pre-MVP

---

## 1. Elevator Pitch

> **For** people and households overwhelmed by daily domestic coordination,
> **who** suffer decision fatigue from the sheer number of microtasks (groceries, meals, chores, pantry, electricity) they repeat every week,
> **HOEM** is a **household coordination web application**
> **that** centralises pantry, shopping list, tasks, weekly menu and energy optimisation into a single system with rule-based smart suggestions,
> **unlike** generic task apps (Todoist, Google Tasks), shopping apps (Bring), or standalone electricity price apps,
> **our product** connects all five domains in a single value loop: what you have at home determines what you cook, what you cook determines what you buy, the electricity price determines when you run appliances, and what needs doing gets distributed among household members.

---

## 2. The Problem

Household management creates a constant mental load that affects wellbeing:

- **+86%** of time spent planning tasks vs. executing them.
- **71%** of mental load falls on a single person in the household.
- **200+** domestic microdecisions per day in an average family.
- **76%** of parents report weekly decision fatigue.
- **Millions of households** are unaware of the electricity price when running the washing machine.

The problem is not a lack of tools (apps exist for everything) — it is **fragmentation**: each decision lives in a different app and none of them talk to each other.

---

## 3. Target Users

### Persona 1 — Laura, 32 (couple without organisation)

Lives with her partner, both work full time. Nobody remembers what is in the fridge. They end up ordering takeaway because they did not plan. She wants to share a shopping list and weekly menus with her partner effortlessly.

*"I spend every Sunday thinking about what to cook. I wish someone would just figure it out for me."*

### Persona 2 — Miguel, 44 (father watching household costs)

Family of 4, he manages household finances. The electricity bill worries him but he does not know when to run appliances to save money. He wants to know the best time to run the washing machine and dishwasher and keep the family pantry under control.

*"I know I can save on electricity but I don't have time to check Red Eléctrica every day."*

### Persona 3 — Sara, 22 (student living alone for the first time)

Student flat, tight budget. She throws food away because she does not track it. She does not know what to cook with what she has. She wants to know what she can cook with available ingredients and shop more efficiently.

*"I open the fridge and have no idea what to do. I end up ordering something and spending money I don't have."*

### Persona 4 — Claudia + Mateo (family with children, primary multi-member segment)

Couple with two children. Claudia coordinates most of the logistics. Mateo wants to help but has no visibility. The burden falls on one person.

*Claudia: "If I don't think of it, it doesn't get done." Mateo: "Just tell me and I'll do it, but I don't know what's missing."*

> **MVP scope decision:** the primary focus is personas 1, 2, 3 and 4, where multi-member value and energy optimisation shine. The data architecture accommodates single-person households without redesign.

---

## 4. Unique Value Proposition

**HOEM turns the home into a coordinated system instead of a collection of disconnected apps.**

Five pillars:

1. **Centralisation** — a single place for all five domains.
2. **Connection** — data flows between modules (pantry ↔ shopping ↔ menu ↔ tasks ↔ energy).
3. **Rule-based intelligence** — the system suggests based on clear rules; the user always decides.
4. **Real energy savings** — integration with ESIOS (Red Eléctrica) for cheap-rate alerts.
5. **Real coordination** — multi-member in real time, not just shared lists.

---

## 5. Non-Goals (what HOEM is NOT)

- A home automation assistant (does not control lights, thermostats or IoT devices).
- An app that automatically buys groceries from supermarkets.
- A general conversational chatbot with generative AI.
- A personal finance or expense-tracking app.
- A family social network (no chat, no feed).
- An app with ML or neural networks in v1 (insufficient historical data).

> **On the roadmap but out of v1:** native mobile app, receipt scanning with camera, supermarket integrations, ML for personalised suggestions.

---

## 6. One-Year Vision

> By May 2027, HOEM will have at least 100 active households coordinating their daily life on a weekly basis. The application will have demonstrated that it reduces the perceived mental load by at least 30% relative to the self-reported baseline after 8 weeks of use, and that at least 40% of active users have changed their appliance usage habits based on electricity price alerts.

This vision is a hypothesis, not a promise. Its purpose is to set the direction.

---

## 7. Known Constraints

- **Resources:** 1 developer, part-time (~10–15 h/week).
- **Budget:** zero euros. Permanently free services only.
- **Infrastructure:** personal home server (Windows 11 + WSL2 + Docker). No VPS.
- **Fixed stack:** Java 21 + Spring Boot 3.x + React 19 + PostgreSQL 16 + Docker.
- **Privacy:** GDPR-compliant from day one.
- **Platform:** responsive web first; native app out of MVP scope.

---

## Changelog

| Version | Date | Changes |
|---|---|---|
| 0.1 | 2026-05-10 | Initial version (Next.js, no energy module) |
| 1.0 | 2026-05-10 | Stack updated to Java/Spring Boot + Docker. Energy module added. User personas expanded |
| 1.1 | 2026-05-12 | Translated to English. No content changes |
