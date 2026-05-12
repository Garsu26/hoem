# HOEM — Low-Fidelity Wireframes (Spec)

**Version:** 1.1
**Date:** 2026-05-12
**Standards applied:**
- **8-point grid system** (Material Design / Apple HIG)
- **Mobile-first design** (Luke Wroblewski, 2011)
- **Jakob Nielsen's 10 Usability Heuristics** (1994, 2020 revision)
- **WCAG 2.2 Level AA**

---

## 1. What is a Low-Fidelity Wireframe and What is It For?

A low-fidelity wireframe represents the structure of a screen without visual styling: only boxes, labels and positions. Its purpose is to validate what information goes where and its hierarchy, before investing time in colour, typography or iconography.

If a lo-fi works, the hi-fi will definitely work. If a lo-fi is not understood, no amount of pretty design will save it.

---

## 2. Grid System and Breakpoints

| Breakpoint | Range | Layout | Grid columns |
|---|---|---|---|
| `sm` (mobile) | 0–639 px | Single column, bottom nav | 4 columns, 16 gutter |
| `md` (tablet) | 640–1023 px | Collapsible sidebar | 8 columns, 16 gutter |
| `lg` (desktop) | 1024–1535 px | Fixed sidebar | 12 columns, 24 gutter |
| `xl` (wide desktop) | 1536+ px | Same, max-width 1440px | 12 columns, 24 gutter |

**Spacing rules (all multiples of 8):**
- Internal container padding: 16 or 24
- Separation between sections: 24 or 32
- Separation between elements within a section: 8 or 16
- Minimum touch target size: 44×44px (Apple HIG) / 48×48dp (Material)

---

## 3. ASCII Notation Legend

| Symbol | Meaning |
|---|---|
| `⚪ / ⚫` | Radio button (unselected / selected) |
| `☐ / ☑` | Checkbox |
| `▼` | Dropdown |
| `⋮` | Contextual menu |
| `←` | Back button |
| `⚠` | Warning indicator (yellow) |
| `🔴` | Critical indicator (red) |
| `●` | OK / active indicator (green) |
| `✓` | Completed |
| `🔍` | Search |
| `[Button]` | Action button |
| `[ ___ ]` | Text field |
| `▓▓▓░░` | Progress bar |
| `📈` | Chart |

---

## 4. Common Components

### 4.1 Mobile app bar (sticky top)

```
┌─────────────────────────────────────┐
│  ☰    García Home ▼       🔔   👤  │
└─────────────────────────────────────┘
```

Height: 56px. Sticky on scroll. Opaque background with subtle shadow on scroll.

### 4.2 Bottom navigation (mobile) — 6 items

```
┌──────────────────────────────────────────┐
│  🏠    🥫    🛒    ✅    🍳    ⚡        │
│ Home  Pantry Shop  Tasks Menu  Energy    │
└──────────────────────────────────────────┘
```

Height: 64px. Active item: top indicator and highlight colour. If 6 items feel cramped in testing, "Energy" moves to a "More" section.

### 4.3 Desktop sidebar

```
┌──────────────┐
│  HOEM        │
│              │
│ García Home▼ │
│              │
│ 🏠 Home    ● │
│ 🥫 Pantry    │
│ 🛒 Shopping  │
│ ✅ Tasks     │
│ 🍳 Menu      │
│ ⚡ Energy    │
│ ─────────    │
│ ⚙ Settings   │
│ ❓ Help      │
│ ─────────    │
│ 👤 Laura     │
│    Admin     │
└──────────────┘
```

Width: 240px. Active item: highlighted background + left border.

### 4.4 FAB (Floating Action Button) — mobile only

56px circle in the bottom-right corner. Contextual to the section: in Pantry opens "Add product", in Shopping "New list", in Tasks "New task", in Energy "New alert".

---

## 5. Wireframes by Flow

### WF-01 · Onboarding — Household type (mobile)

```
┌─────────────────────────────────────┐
│  ←                          02 / 05 │
├─────────────────────────────────────┤
│                                     │
│  Who lives here?                    │
│  So we can adapt HOEM to your life. │
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Living alone                 ⚪ ││
│  │ A flat at my own pace           ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │ Couple                       ⚪ ││
│  │ Two shared schedules            ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │ Family with children         ⚫ ││
│  │ More logistics, more help       ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │ Flatmates                    ⚪ ││
│  │ Roommates and shared rules      ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌──────┐               ┌──────────┐│
│  │ Back │               │ Continue ││
│  └──────┘               └──────────┘│
└─────────────────────────────────────┘
```

Nielsen heuristic #1: the "02/05" counter shows progress. 5 steps because optional energy configuration is added.

---

### WF-02 · Dashboard / Home (mobile)

```
┌─────────────────────────────────────┐
│  ☰   García Home ▼       🔔   👤   │
├─────────────────────────────────────┤
│                                     │
│  Good afternoon, Laura              │
│                                     │
│  ┌─────────┬─────────┬─────────┐    │
│  │  3      │   5     │  0.09   │    │
│  │ tasks   │ products│ €/kWh   │    │
│  │ today   │ low stk.│ NOW   ● │    │
│  └─────────┴─────────┴─────────┘    │
│                                     │
│  ⚡ Good time for appliances        │
│  Cheap rate until 16:00             │
│  [See recommendations]              │
│                                     │
│  Pantry                    See all  │
│  ┌─────────────────────────────────┐│
│  │ ⚠ Milk — expires tomorrow    ⋮ ││
│  │ 🔴 Sliced bread — low stock   ⋮ ││
│  └─────────────────────────────────┘│
│                                     │
│  Tasks today               See all  │
│  ┌─────────────────────────────────┐│
│  │ ☐ Run washing machine  08:30  ⋮ ││
│  │ ☐ Take out recycling   19:00  ⋮ ││
│  └─────────────────────────────────┘│
│                                     │
│  Active shopping list      See all  │
│  ┌─────────────────────────────────┐│
│  │ Weekly shop · 12 items          ││
│  │ ▓▓▓▓░░░░░░  4 of 12 purchased  ││
│  └─────────────────────────────────┘│
│                                              ╔═══╗
├─────────────────────────────────────         ║ + ║
│  🏠● 🥫   🛒   ✅   🍳   ⚡         │        ╚═══╝
└─────────────────────────────────────┘
```

**Design decisions:**
- **Prominent energy widget** on the dashboard when a cheap rate is active. If no cheap rate, the widget shows the current price neutrally.
- **€/kWh in stats** alongside tasks and low stock — the three most actionable data points of the day.
- **Contextual energy banner** only appears when the price is below the configured threshold. Not intrusive during normal or expensive periods.

---

### WF-03 · Pantry — Product list (mobile)

```
┌─────────────────────────────────────┐
│  ☰   Pantry               🔔   👤  │
├─────────────────────────────────────┤
│  🔍 Search products…                │
├─────────────────────────────────────┤
│  [All] [Low stock] [Expiring]       │
├─────────────────────────────────────┤
│  DAIRY                              │
│  ┌─────────────────────────────────┐│
│  │ Whole milk           2 L     ⋮  ││
│  │ ⚠ Expires in 1 day              ││
│  ├─────────────────────────────────┤│
│  │ Natural yoghurt      6 units ⋮  ││
│  └─────────────────────────────────┘│
│  BAKERY                             │
│  ┌─────────────────────────────────┐│
│  │ 🔴 Sliced bread      0 units ⋮  ││
│  │ Below minimum threshold          ││
│  └─────────────────────────────────┘│
│  FRUIT & VEG                        │
│  ┌─────────────────────────────────┐│
│  │ Tomatoes             1 kg    ⋮  ││
│  └─────────────────────────────────┘│
│                                              ╔═══╗
├─────────────────────────────────────         ║ + ║
│  🏠  🥫●  🛒   ✅   🍳   ⚡        │        ╚═══╝
└─────────────────────────────────────┘
```

---

### WF-04 · Shopping — Active list (mobile)

```
┌─────────────────────────────────────┐
│  ←   Weekly shop          🔔   👤  │
│      12 items · 4 purchased         │
├─────────────────────────────────────┤
│  ▓▓▓▓░░░░░░░░  4 / 12              │
├─────────────────────────────────────┤
│  DAIRY                              │
│  ┌─────────────────────────────────┐│
│  │ ☑ Whole milk         2 L     ⋮  ││
│  │ ☑ Natural yoghurt    6 units ⋮  ││
│  ├─────────────────────────────────┤│
│  │ ☐ Butter             250 g   ⋮  ││
│  └─────────────────────────────────┘│
│  BAKERY                             │
│  ┌─────────────────────────────────┐│
│  │ ☐ Sliced bread       1 unit  ⋮  ││
│  │ ☐ Baguette           2 units ⋮  ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ + Add item manually             ││
│  └─────────────────────────────────┘│
│                                     │
│  [Mark shopping as complete]        │
│                                              ╔═══╗
├─────────────────────────────────────         ║ + ║
│  🏠  🥫   🛒●  ✅   🍳   ⚡        │        ╚═══╝
└─────────────────────────────────────┘
```

---

### WF-05 · Energy — Current price (mobile)

```
┌─────────────────────────────────────┐
│  ☰   Energy               🔔   👤  │
├─────────────────────────────────────┤
│  [Now●] [24h forecast] [Alerts]     │
├─────────────────────────────────────┤
│                                     │
│         0.09 €/kWh                  │
│         CHEAP RATE ●                │
│                                     │
│  Until 16:00 · Saving vs avg: 0.03  │
│                                     │
│  Recommendations for now            │
│  ┌─────────────────────────────────┐│
│  │ 🌀 Washing machine              ││
│  │ Best time until 16:00           ││
│  │ Estimated saving: 0.18 €        ││
│  ├─────────────────────────────────┤│
│  │ 🍽 Dishwasher                   ││
│  │ Best time until 16:00           ││
│  │ Estimated saving: 0.12 €        ││
│  └─────────────────────────────────┘│
│                                     │
├─────────────────────────────────────┤
│  🏠  🥫   🛒   ✅   🍳  ⚡●        │
└─────────────────────────────────────┘
```

**Design decisions:**
- **Large price** as the first element — the user has the answer before reading anything else.
- **"CHEAP RATE"** label with colour — green for cheap, amber for medium, red for expensive.
- **Appliance recommendations only when there are cheap rates** — no recommendations during medium/expensive periods to avoid noise.

---

### WF-06 · Energy — 24h forecast (mobile)

```
┌─────────────────────────────────────┐
│  ☰   Energy               🔔   👤  │
├─────────────────────────────────────┤
│  [Now] [24h forecast●] [Alerts]     │
├─────────────────────────────────────┤
│                                     │
│  Today · Monday 11 May              │
│                                     │
│  Lowest price:  0.06 €  03:00       │
│  Highest price: 0.21 €  19:00       │
│                                     │
│  ┌─────────────────────────────────┐│
│  │  📈                             ││
│  │  Hourly bar chart               ││
│  │  Green:  cheap band             ││
│  │  Amber:  medium band            ││
│  │  Red:    expensive band         ││
│  │                                 ││
│  │  00 01 02 03 04 ... 22 23       ││
│  └─────────────────────────────────┘│
│                                     │
│  Cheap periods today                │
│  ┌─────────────────────────────────┐│
│  │ 00:00 - 08:00   < 0.10 €/kWh   ││
│  │ 14:00 - 16:00   < 0.10 €/kWh   ││
│  └─────────────────────────────────┘│
│                                     │
├─────────────────────────────────────┤
│  🏠  🥫   🛒   ✅   🍳  ⚡●        │
└─────────────────────────────────────┘
```

**Design decisions:**
- **Colour-coded bar chart** — green/amber/red by price band. User sees periods at a glance without reading numbers.
- **Text summary of cheap periods** below the chart — for users who prefer text over visuals.
- **Min/max price for the day** at the top — quick context before viewing the chart.

---

### WF-07 · Energy — Configure alert (mobile, modal)

```
┌─────────────────────────────────────┐
│  ✕   New alert               Save  │
├─────────────────────────────────────┤
│                                     │
│  Notify me when the price drops     │
│  below                              │
│                                     │
│  ┌─────────────────────────────────┐│
│  │  0.10                     €/kWh ││
│  └─────────────────────────────────┘│
│                                     │
│  Active days                        │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐│
│  │M●│ │T●│ │W●│ │T●│ │F●│ │S │ │S ││
│  └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘│
│  (Mon–Fri selected)                 │
│                                     │
│  Notify via                         │
│  ☑ Email                            │
│  ☑ In-app notification              │
│                                     │
│  ─────────────────────────────────  │
│                                     │
│  💡 Average price today: 0.12 €/kWh│
│     With this threshold you will    │
│     receive alerts for ~6h/day      │
│                                     │
└─────────────────────────────────────┘
```

**Design decisions:**
- **Alert frequency preview** ("you will receive alerts for ~6h/day") — user calibrates whether the threshold is too low or too high before saving. Nielsen heuristic #10 (Help and documentation).
- **Days of the week** — many users do not want alerts on weekends if they are out of the house.
- **Pre-filled threshold** at 0.10 €/kWh (low average price in Spain). User can adjust.

---

### WF-08 · Tasks — Today view (mobile)

```
┌─────────────────────────────────────┐
│  ☰   Tasks                🔔   👤  │
├─────────────────────────────────────┤
│  [Today●] [Week] [Month]            │
├─────────────────────────────────────┤
│                                     │
│  Today · Monday 11 May              │
│  5 tasks · 2 for you                │
│                                     │
│  FOR YOU                            │
│  ┌─────────────────────────────────┐│
│  │ ☐ Run washing machine  08:30  ⋮ ││
│  │   💡 Good time — cheap rate     ││
│  ├─────────────────────────────────┤│
│  │ ☐ Take out recycling   19:00  ⋮ ││
│  └─────────────────────────────────┘│
│                                     │
│  FOR MIGUEL                         │
│  ┌─────────────────────────────────┐│
│  │ ☐ Water plants         11:30  ⋮ ││
│  ├─────────────────────────────────┤│
│  │ ☐ Review accounts      20:00  ⋮ ││
│  └─────────────────────────────────┘│
│                                     │
│  COMPLETED TODAY                    │
│  ┌─────────────────────────────────┐│
│  │ ✓ Weekly shop                   ││
│  └─────────────────────────────────┘│
│                                              ╔═══╗
├─────────────────────────────────────         ║ + ║
│  🏠  🥫   🛒  ✅●  🍳   ⚡         │        ╚═══╝
└─────────────────────────────────────┘
```

**Design decisions:**
- **Energy hint in tasks** ("💡 Good time — cheap rate") when a task involves an appliance and coincides with a cheap rate. Cross-module connection on the same screen.
- **Tasks completed today** visible in a separate section at the bottom — reinforces the sense of productivity.

---

### WF-09 · Menu — Weekly plan (desktop)

```
┌────────────┬────────────────────────────────────────────────────────┐
│ HOEM       │  Weekly menu                    Week 11–17 May         │
│            │                                                        │
│ García H▼  │          │  MON  │  TUE  │  WED  │  THU  │  FRI      │
│            │ LUNCH     │Lentil │Lemon  │Pasta  │Lasagne│Baked      │
│ 🏠 Home    │           │stew   │chicken│pesto  │veggie │salmon     │
│ 🥫 Pantry  │           │ [↻]  │ [↻]  │ [↻]  │ [↻]  │ [↻]       │
│ 🛒 Shopping│ DINNER    │Cream  │French │Caesar │Miso   │Home       │
│ ✅ Tasks   │           │squash │omelet.│salad  │soup   │pizza      │
│ 🍳 Menu  ● │           │ [↻]  │ [↻]  │ [↻]  │ [↻]  │ [↻]       │
│ ⚡ Energy  │                                                        │
│ ─────────  │  Using from pantry: 🥬 squash · 🧀 cheese · 🌿 basil  │
│ ⚙ Settings │                                                        │
│            │  [Suggest recipes]  [Generate shopping list]           │
└────────────┴────────────────────────────────────────────────────────┘
```

---

## 6. State Patterns (Cross-cutting)

### 6.1 Empty state

```
┌─────────────────────────────────────┐
│                                     │
│         [Soft illustration]         │
│                                     │
│   No energy alerts                  │
│   Set up your first alert to know   │
│   when electricity is cheapest.     │
│                                     │
│      ┌──────────────────────┐       │
│      │  Create first alert  │       │
│      └──────────────────────┘       │
│                                     │
└─────────────────────────────────────┘
```

### 6.2 Loading state (skeleton)

```
┌─────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓░░░░░                    │
│  ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░               │
│  ┌─────────────────────────────────┐│
│  │ ▓▓▓▓▓▓░░░░░░░░░░░░░░░░         ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

Skeleton screens instead of spinners — reduce perceived waiting time.

### 6.3 Error state — ESIOS service unavailable

```
┌─────────────────────────────────────┐
│                                     │
│             ⚠                       │
│                                     │
│    Energy data may be outdated      │
│                                     │
│    Using prices from 3 hours ago.   │
│    The Red Eléctrica API is not     │
│    responding right now.            │
│                                     │
│         ┌─────────────┐             │
│         │    Retry    │             │
│         └─────────────┘             │
│                                     │
└─────────────────────────────────────┘
```

Never a blank screen. Always fallback data + explanation in plain language.

---

## 7. Open Decisions to Validate in Testing

1. **6 items in bottom nav:** uncomfortable on small mobile? Hypothesis: fits fine at 375px. Validate with real devices.
2. **Energy hint in tasks (WF-08):** useful or noise? Hypothesis: useful for active energy users. Validate with beta users.
3. **Large price in energy:** does the user immediately understand whether it is cheap or expensive? Hypothesis: colour + label ("CHEAP RATE") is sufficient. Validate.
4. **Estimated saving in euros:** does it create incorrect expectations? Always add the word "estimated" to avoid misunderstandings.
5. **Bar chart in forecast:** sufficient with colours or does each bar need a price label? Hypothesis: only on tap (tooltip).

---

## Changelog

| Version | Date | Changes |
|---|---|---|
| 0.1 | 2026-05-10 | Initial version (9 wireframes, no energy module) |
| 1.0 | 2026-05-10 | Bottom nav updated to 6 items. WF-05, WF-06 and WF-07 added (energy module). Energy hint added to WF-08 (tasks). Energy widget added to WF-02 (dashboard) |
| 1.1 | 2026-05-12 | Translated to English. WF-05 and WF-06 updated to reflect cheap/medium/expensive price bands (amber added for medium). Labels translated throughout |
