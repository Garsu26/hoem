# HOEM — User Stories

**Version:** 1.1
**Date:** 2026-05-12
**Standards applied:**
- Mike Cohn format (*User Stories Applied*, 2004): "As a [role] I want [action] so that [benefit]"
- INVEST criteria (Independent, Negotiable, Valuable, Estimable, Small, Testable)
- Acceptance criteria in Gherkin (Given–When–Then) — BDD standard

---

## Structure

Stories are grouped by epic, one per module. Each story has:

- **ID:** `US-[MODULE]-[NUMBER]`
- **Traceability:** reference to functional requirement in `01_prd.md`
- **Estimate:** story points on Fibonacci scale (1, 2, 3, 5, 8, 13)
- **Priority:** MoSCoW (Must / Should / Could)

---

## Epic 1 — Authentication and Households (CORE)

### US-CORE-001 — Register with email
**Traceability:** FR-CORE-001 · **Priority:** MUST · **Estimate:** 3

> As a visitor, I want to register with email and password so that I can access HOEM.

```gherkin
Scenario: Successful registration
  Given I am on the registration page
  When I enter a valid email and a password of at least 8 characters
  And I accept the privacy policy
  And I click "Create account"
  Then my account is created
  And I receive a verification email
  And I am redirected to onboarding

Scenario: Email already registered
  Given an account exists with email "ana@example.com"
  When I try to register with that same email
  Then the system shows "This email is already registered"
  And no new account is created

Scenario: Weak password
  Given I am on the registration page
  When I enter a password of fewer than 8 characters
  Then the system shows the minimum requirements
  And the "Create account" button remains disabled
```

---

### US-CORE-002 — Create household
**Traceability:** FR-CORE-004 · **Priority:** MUST · **Estimate:** 2

> As a newly registered user, I want to create a household so that I can start using the app.

```gherkin
Scenario: Create first household
  Given I have just registered and have no households
  When I enter a household name
  And I select the type (Single / Couple / Family / Flatmates)
  And I click "Create household"
  Then the household is created
  And I am assigned as administrator
  And I am redirected to the household dashboard
```

---

### US-CORE-003 — Invite members
**Traceability:** FR-CORE-005 · **Priority:** MUST · **Estimate:** 5

> As a household admin, I want to invite members so that coordination can be shared.

```gherkin
Scenario: Share invite code
  Given I am admin of a household
  When I go to "Members" and click "Invite"
  Then the system displays the household's 6-character invite code
  And I can share it or disable it

Scenario: New user joins with code
  Given a user enters the 6-character invite code
  When they submit the join request
  Then the admin receives a notification to approve or reject
  And upon approval the user becomes a member with role "member"

Scenario: Admin rejects join request
  Given there is a pending join request
  When the admin clicks "Reject"
  Then the requesting user is not added to the household
  And they receive a notification that their request was declined

Scenario: Code disabled
  Given the admin has disabled the invite code
  When someone tries to use it
  Then the system shows "This invite code is not active"
  And suggests contacting the household admin
```

---

### US-CORE-004 — Password recovery
**Traceability:** FR-CORE-003 · **Priority:** MUST · **Estimate:** 2

> As a user who has forgotten their password, I want to recover it by email so that I do not lose access.

```gherkin
Scenario: Request password recovery
  Given I am on the login screen
  When I click "Forgot my password" and enter my email
  Then I receive an email with a recovery link valid for 1 hour
  And the link takes me to a screen to set a new password
```

---

## Epic 2 — Pantry

### US-PANT-001 — Add product
**Traceability:** FR-PANT-001 · **Priority:** MUST · **Estimate:** 3

> As a household member, I want to add products to the pantry so that I have an inventory of what is at home.

```gherkin
Scenario: Add complete product
  Given I am in the Pantry module
  When I click "Add product"
  And I fill in name, category, quantity and unit
  And I optionally add an expiry date
  And I click "Save"
  Then the product appears in the list
  And all household members see it updated

Scenario: Empty name
  Given I am on the add product form
  When I leave the name empty
  Then the "Save" button is disabled
  And the message "Name is required" is shown
```

---

### US-PANT-002 — Expiry alerts
**Traceability:** FR-PANT-006 · **Priority:** SHOULD · **Estimate:** 3

> As a household member, I want to see which products are about to expire so that I do not waste food.

```gherkin
Scenario: Product expiring in 3 days
  Given I have "Yoghurt" with an expiry date in 2 days
  When I open the dashboard
  Then I see an alert "Yoghurt expires in 2 days"
  And the product is highlighted in the list

Scenario: Product already expired
  Given I have "Sliced bread" with a past expiry date
  Then it is marked as "Expired" in red
  And I am prompted to remove it or mark it as thrown away
```

---

### US-PANT-003 — Quick quantity decrement
**Traceability:** FR-PANT-007 · **Priority:** SHOULD · **Estimate:** 2

> As a household member, I want to decrement a product's quantity with a single tap so that I keep the pantry updated effortlessly.

```gherkin
Scenario: Decrement quantity
  Given I have "Milk" with quantity 2 in the pantry
  When I tap the "−" button on the product card
  Then the quantity drops to 1
  And the change is synced for all members

Scenario: Reaching zero stock
  Given I have "Bread" with quantity 1
  When I tap "−"
  Then the product is marked as "Out of stock"
  And a prompt appears "Add to shopping list?"
```

---

## Epic 3 — Shopping

### US-SHOP-001 — Generate list from low stock
**Traceability:** FR-SHOP-002 · **Priority:** MUST · **Estimate:** 5

> As a household member, I want the shopping list to be built automatically from what is running low so that I do not have to think about it.

```gherkin
Scenario: Automatic suggestion
  Given I have 5 products with low stock
  When I go to "Shopping list" and click "Generate list"
  Then the system adds those 5 products to a new list
  And I can edit them before confirming
```

---

### US-SHOP-002 — Mark item as purchased
**Traceability:** FR-SHOP-004 · **Priority:** MUST · **Estimate:** 3

> As a household member, I want to mark purchased products from the supermarket so that the pantry updates itself.

```gherkin
Scenario: Mark purchased updates pantry
  Given I have "Milk x2" on the list
  When I tap the check
  Then the item is crossed out on the list
  And the quantity of "Milk" in the pantry increases by 2
  And a toast "Pantry updated" appears

Scenario: Mark purchased without updating pantry
  Given I have "Light bulb" on the list
  When I tap the check
  And the product does not exist in the pantry
  Then the item is marked as purchased
  And the pantry is not modified
```

---

### US-SHOP-003 — Real-time synchronisation
**Traceability:** FR-SHOP-005 · **Priority:** MUST · **Estimate:** 5

> As a household member, I want to see list changes in real time so that we do not buy the same thing twice when several of us go to the supermarket.

```gherkin
Scenario: Two members edit at the same time
  Given Laura and Miguel both have the same list open
  When Laura marks "Milk" as purchased
  Then within 10 seconds Miguel sees "Milk" crossed out
  Without needing to refresh the page
```

> **Technical note:** implemented with conditional polling every 5s + optimistic UI for the member making the change. Perceived latency is minimal for the actor; observers see changes in ≤ 5s.

---

## Epic 4 — Tasks

### US-TASK-001 — Create task
**Traceability:** FR-TASK-001 · **Priority:** MUST · **Estimate:** 2

> As a household member, I want to create a task so that it is not forgotten.

```gherkin
Scenario: Create simple task
  Given I am in the Tasks module
  When I click "New task"
  And I fill in title, date and priority
  And I click "Save"
  Then the task appears on the dashboard
```

---

### US-TASK-002 — Recurring task
**Traceability:** FR-TASK-002 · **Priority:** MUST · **Estimate:** 5

> As a household member, I want to create recurring tasks so that I do not have to recreate them every week.

```gherkin
Scenario: Weekly task
  Given I create a task with recurrence "Weekly, on Mondays"
  When I complete today's task
  Then the next instance is scheduled for the following Monday
  And the history keeps a record of completed instances
```

---

### US-TASK-003 — Fair distribution
**Traceability:** FR-TASK-006 · **Priority:** SHOULD · **Estimate:** 8

> As a household member, I want tasks to be distributed fairly among members so that nobody carries all the load.

```gherkin
Scenario: Automatic distribution suggestion
  Given there are 12 pending tasks and 3 active members
  When I click "Suggest distribution"
  Then the system assigns 4 tasks to each member (round-robin in v1)
  And shows a preview I can accept or adjust manually
```

---

## Epic 5 — Menu

### US-MENU-001 — Create recipe
**Traceability:** FR-MENU-001 · **Priority:** MUST · **Estimate:** 3

> As a household member, I want to save my usual recipes so that I can reuse them in the weekly plan.

```gherkin
Scenario: Create recipe with ingredients
  Given I am in the Menu module
  When I create a recipe "Lentil stew"
  And I add numbered preparation steps
  And I add ingredients with quantities and units
  And I set prep time, cook time, servings and category
  Then the recipe is saved and available for the plan
```

---

### US-MENU-002 — Weekly plan
**Traceability:** FR-MENU-002, FR-MENU-003 · **Priority:** MUST · **Estimate:** 5

> As a household member, I want to plan the week's meals so that I do not have to improvise every evening.

```gherkin
Scenario: Assign recipe to a slot
  Given I am in the weekly plan
  And I see a 7-day × 2-slot grid (lunch/dinner)
  When I tap an empty slot and choose a recipe
  Then the recipe is assigned
  And the missing ingredients relative to the pantry are shown
```

---

### US-MENU-003 — Pantry-based suggestions
**Traceability:** FR-MENU-004 · **Priority:** SHOULD · **Estimate:** 5

> As a household member, I want the system to suggest meals based on what I already have so that I reduce food waste.

```gherkin
Scenario: Rule-based suggestion
  Given I have in the pantry: chicken, rice, onion, tomato
  When I click "Suggest meal with what I have"
  Then the system returns recipes whose ingredients
  mostly match the current stock
  And each suggestion shows which ingredients are missing
```

> **Note:** in v1 suggestions are rule-based ingredient matching without ML. The system compares recipe ingredients with pantry stock and sorts by match percentage.

---

### US-MENU-004 — Generate list from plan
**Traceability:** FR-MENU-005 · **Priority:** MUST · **Estimate:** 5

> As a household member, I want to generate the shopping list from my weekly plan so that I do not have to calculate it mentally.

```gherkin
Scenario: Plan generates list
  Given I have the weekly plan with 7 recipes assigned
  And my pantry covers some ingredients
  When I click "Generate shopping list"
  Then the system creates a list only with missing ingredients
  And shows "Ready for 7 meals, X products missing"
```

---

## Epic 6 — Energy ⚡

### US-ENE-001 — View real-time electricity price
**Traceability:** FR-ENE-001, FR-ENE-002 · **Priority:** MUST · **Estimate:** 5

> As a user, I want to see the current electricity price so that I know whether it is a good time to run appliances.

```gherkin
Scenario: Real-time query
  Given I am in the Energy module
  When I open the main screen
  Then I see the current price in €/kWh (PVPC)
  And I see whether I am in a cheap, medium or expensive band
  And the data is less than 1 hour old

Scenario: ESIOS unavailable
  Given the ESIOS API is not responding
  When I open the Energy module
  Then I see yesterday's data with the notice "Data may be outdated"
  And the app does not crash or show a critical error
```

---

### US-ENE-002 — 24-hour price forecast
**Traceability:** FR-ENE-003 · **Priority:** MUST · **Estimate:** 3

> As a user, I want to see the price forecast for the next 24 hours so that I can plan when to use appliances.

```gherkin
Scenario: View daily forecast
  Given I am in the Energy module
  When I tap "View forecast"
  Then I see a chart with hourly prices for today and tomorrow
  And cheap hours are highlighted in green
  And expensive hours in red
  And medium hours in amber
```

---

### US-ENE-003 — Cheap-rate alerts
**Traceability:** FR-ENE-004 · **Priority:** MUST · **Estimate:** 5

> As a user, I want to receive an alert when the cheapest rate of the day starts so that I can take advantage of it.

```gherkin
Scenario: Cheap-rate alert
  Given I have alerts enabled
  When the system detects a period with price below the configured threshold
  Then I receive an email "Good time to run appliances"
  And the energy module shows an in-app notification

Scenario: Configure alert threshold
  Given I am in Energy module settings
  When I set the threshold to 0.10 €/kWh
  Then I will only receive alerts when the price is below that value
```

---

### US-ENE-004 — Appliance recommendations
**Traceability:** FR-ENE-005 · **Priority:** MUST · **Estimate:** 3

> As a user, I want to see a recommendation for when to run the washing machine today based on the forecast price.

```gherkin
Scenario: Daily recommendation
  Given I am in the Energy module
  When I check today's recommendations
  Then I see "Best time for washing machine: 14:00–16:00 (0.08 €/kWh)"
  And I see "Best time for dishwasher: 02:00–04:00 (0.06 €/kWh)"
  With a night option and a daytime option for each appliance

Scenario: Configurable appliances
  Given I am in Energy module settings
  When I add "Tumble dryer" with power 2000W
  Then recommendations include the tumble dryer
  And estimated saving is calculated with its actual wattage
```

---

### US-ENE-005 — Estimated savings history
**Traceability:** FR-ENE-008 · **Priority:** COULD · **Estimate:** 5

> As a user, I want to see how much I have saved by using cheap rates so that I am motivated to keep using them.

```gherkin
Scenario: View monthly saving
  Given I have been using alerts for a month
  When I go to "Savings statistics"
  Then I see the estimated saving in euros for the current month
  And the comparison vs running appliances during expensive hours
```

---

## MVP Estimation Summary

| Epic | Story points | Notes |
|---|---|---|
| CORE (auth + households) | 12 | |
| Pantry | 8 | |
| Shopping | 13 | Synchronisation reduced to 5 pts (polling vs WS) |
| Tasks | 15 | Fair distribution optional for v1 |
| Menu | 18 | Rule-based suggestions, no ML |
| Energy | 21 | New module, includes ESIOS integration |
| **Visible total** | **~87 SP** | + ~25–30 SP for technical tasks (setup, CI, docker, testing) |

At 8–12 SP/week part-time: **9–14 weeks of pure development**, confirming the 4–6 month timeline including planning, design, deployment and polish.

---

## Changelog

| Version | Date | Changes |
|---|---|---|
| 0.1 | 2026-05-10 | Initial version (Next.js, no energy module) |
| 1.0 | 2026-05-10 | Stack updated. Epic 6 (Energy) added with 5 stories. List synchronisation adjusted to polling |
| 1.1 | 2026-05-12 | Translated to English. US-CORE-003 updated to reflect 6-char invite code with admin approval flow. US-MENU-001 updated to include numbered steps, prep/cook time, servings and category. US-ENE-001/002 updated to reflect cheap/medium/expensive price bands |
