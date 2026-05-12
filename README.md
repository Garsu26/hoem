# HOEM
 
> Household coordination app — pantry, shopping, tasks, weekly menu and energy optimisation in a single connected system.
 
[![CI - auth-service](https://github.com/your-username/hoem/actions/workflows/ci-auth.yml/badge.svg)](https://github.com/your-username/hoem/actions)
[![CI - frontend](https://github.com/your-username/hoem/actions/workflows/ci-frontend.yml/badge.svg)](https://github.com/your-username/hoem/actions)
 
---
 
## What is HOEM?
 
HOEM turns the home into a coordinated system instead of a collection of disconnected apps. It connects five domains in a single value loop:
 
- **Pantry** — know what you have at home and get expiry alerts
- **Shopping** — auto-generate lists from low stock or weekly plan
- **Tasks** — assign and track household chores with fair distribution
- **Menu** — plan the week's meals and generate the shopping list
- **Energy** — know when electricity is cheapest and get appliance recommendations
## Tech Stack
 
| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3.x (microservices) |
| Frontend | React 19 + Vite + Zustand + TanStack Query |
| Database | PostgreSQL 16 (single instance, schemas per service) |
| Gateway | Spring Cloud Gateway |
| Migrations | Flyway (per service) |
| Infrastructure | Docker + Docker Compose |
| Exposure | Cloudflare Tunnel |
| Email | Resend |
| Analytics | PostHog |
 
## Quick Start
 
```bash
# 1. Clone the repository
git clone https://github.com/your-username/hoem.git
cd hoem
 
# 2. Copy and fill in environment variables
cp .env.example .env
 
# 3. Start all services
docker-compose up -d
 
# 4. Check everything is running
docker-compose ps
 
# 5. Open the app
open http://localhost:3000
```
 
Full setup instructions in the [Wiki](https://github.com/your-username/hoem/wiki/Local-Setup).
 
## Project Structure
 
```
hoem/
├── api-gateway/          Spring Cloud Gateway
├── auth-service/         Auth, households, members
├── pantry-service/       Products, categories, stock
├── shopping-service/     Shopping lists, items
├── tasks-service/        Tasks, assignments, recurrence
├── menu-service/         Recipes, weekly plan, suggestions
├── energy-service/       ESIOS integration, price cache, alerts
├── notification-service/ Email and in-app notifications
├── frontend/             React 19 SPA
├── docs/                 Project documentation
└── scripts/              DB init and seed scripts
```
 
## Documentation
 
All documentation lives in the [`docs/`](./docs/) folder:
 
| Document | Description |
|---|---|
| [Vision](./docs/00_vision.md) | Product vision and value proposition |
| [PRD](./docs/01_prd.md) | Full product requirements |
| [User Stories](./docs/02_user_stories.md) | All user stories with acceptance criteria |
| [Success Metrics](./docs/03_success_metrics.md) | HEART, AARRR, OKRs |
| [Information Architecture](./docs/04_information_architecture.md) | Sitemap, URLs, navigation |
| [User Flows](./docs/05_user_flows.md) | Screen-by-screen user flows |
| [Wireframes](./docs/06_wireframes_spec.md) | Low-fidelity wireframe specs |
| [C4 Architecture](./docs/08_c4_architecture.md) | System, container and component diagrams |
| [ADRs](./docs/09_adr.md) | Architecture Decision Records |
| [Data Model](./docs/10_data_model.md) | PostgreSQL schemas and Flyway migrations |
 
## Contributing
 
See [CONTRIBUTING.md](./CONTRIBUTING.md) for commit conventions, branch strategy and PR process.
 
## License
 
Private — all rights reserved.
