# Package Refactor Plan

## Objective
- Reorganize backend packages so each domain is split by responsibility.
- Preserve existing behavior and avoid touching unrelated user changes.
- Verify the refactor with a backend test/build pass.

## Scope
- Target: `backend/src/main/java/com/derivops/mvp`
- Primary focus: domain packages that currently mix controller/service/entity/repository/dto in one directory
- Out of scope: feature changes, UI redesign, unrelated workspace cleanup

## Target Structure
- `api`: controllers
- `application`: services and use-case orchestration
- `domain`: entities, enums, core models
- `dto`: request/response models
- `infrastructure`: repositories and implementation classes
- `common`: shared exception/security/query utilities with clearer sub-packages where needed

## Execution Steps
1. Add working plan/task documents for this refactor.
2. Move backend classes into role-based sub-packages inside each domain.
3. Update package declarations and imports across production and test code.
4. Build and test the backend to catch package-scan or import regressions.
5. Record completion state in `task.md`.

## Risks
- Large package moves can break Spring component scanning if imports are missed.
- JPA entity/repository package moves can break Querydsl/generated source references until rebuilt.
- The worktree is already dirty, so only files directly needed for this refactor should be changed.

## Stock Purchase Extension

### Objective
- Add a stock purchase transaction flow that leaves an operational trail in position, ledger, and journal domains.

### Scope
- Target: `backend/src/main/java/com/derivops/mvp`
- In scope: stock purchase create/list API, stock position query, ledger entry query, journal entry query, seed/test coverage
- Out of scope: stock sell/PnL/valuation processing, frontend screen expansion

### Execution Steps
1. Add stock purchase, stock position, ledger entry, and journal entry domain classes and repositories.
2. Implement stock purchase application flow to validate account/trade inputs and persist the purchase.
3. Upsert stock position and generate ledger/journal records in the same transaction.
4. Seed one sample stock purchase and verify the flow with backend smoke tests.

## Domain Terms Extension

### Objective
- Define key business terms in Korean and keep the glossary in the database so Dashboard and documents share one source of truth.

### Scope
- Target: glossary domain in backend, Dashboard rendering in frontend, glossary docs in `docs/`
- In scope: read-only glossary API, seed data, Dashboard section, documentation sync
- Out of scope: glossary admin CRUD UI

### Execution Steps
1. Add a DB-backed `domain_terms` model and read API.
2. Seed Korean business terms grouped by domain.
3. Render glossary cards on Dashboard from the API.
4. Add the same terms to a dedicated document and update indexes.

## Securities / Portfolio Extension

### Objective
- Expand MVP coverage from operational cash workflows into stock, securities, and portfolio monitoring flows for day-to-day operations.

### Scope
- Target: stock/securities pages in frontend, portfolio read API in backend, menu/docs alignment
- In scope: portfolio overview API, stock purchase/position UI, menu exposure, documentation sync
- Out of scope: stock sell, market valuation, realized/unrealized PnL

### Execution Steps
1. Add read-only portfolio overview API by account.
2. Add stock purchase and stock position pages in frontend.
3. Add portfolio page and new menus/routes/tooltips.
4. Update docs and verify backend/frontend builds.

## Domain Glossary Data Expansion

### Objective
- Enrich the DB-backed glossary with practical trading and operations terms so Dashboard can explain the business domain in Korean.

### Scope
- Target: glossary seed data in backend and synchronized glossary documentation
- In scope: additional terms for 주문, 체결, 정산, 거래소, 브로커, 계좌상태, 장중 운영 기준 용어
- Out of scope: glossary CRUD UI, translation workflow, external terminology source integration

### Execution Steps
1. Review current glossary coverage and identify missing trading/operations terms.
2. Add additional seeded glossary entries to `domain_terms`.
3. Sync the human-readable glossary document with the DB seed.
4. Run backend tests to verify the seed changes do not break startup.

## Domain Glossary Menu Exposure

### Objective
- Expose the DB-backed glossary as a dedicated menu and page so users can open the domain dictionary directly from the sidebar.

### Scope
- Target: backend menu seed, frontend route, sidebar icon, glossary page, related docs
- In scope: `도메인 용어집` menu item, read-only glossary page, tooltip text
- Out of scope: glossary CRUD, role policy redesign

### Execution Steps
1. Add a DB menu entry for the glossary page.
2. Add frontend route/resource and a dedicated glossary page.
3. Wire tooltip/icon/description metadata.
4. Verify backend/frontend build paths.

## AI Stock Recommendation Extension

### Objective
- Add an Ollama + Spring AI based stock recommendation feature that uses account portfolio context and operator inputs to produce structured recommendation drafts.

### Scope
- Target: backend AI recommendation API, frontend recommendation page, menu/docs sync
- In scope: Spring AI/Ollama integration, stub provider for tests, recommendation request/response, operational disclaimer UI
- Out of scope: live market data ingestion, auto-order placement, investment suitability engine

### Execution Steps
1. Add Spring AI Ollama dependency and application properties.
2. Implement recommendation service with Ollama provider and test stub provider.
3. Add recommendation API, menu seed, frontend page, and route.
4. Update documents and run backend/frontend verification.

## FX Rate / Conversion Extension

### Objective
- Add practical MVP support for exchange rates and FX conversion so operators can manage rates, preview converted amounts, and create FX requests with quoted rates attached.

### Scope
- Target: exchange rate backend domain, FX request enrichment, frontend rate screen and FX request preview flow
- In scope: exchange rate master, quote API, FX request rate fields, exchange-rate menu/page, docs sync
- Out of scope: live market feed ingestion, broker execution price reconciliation, PnL revaluation

### Execution Steps
1. Add exchange-rate entity, repository, service, and API.
2. Attach quoted rate and expected receive amount to FX requests.
3. Add frontend `Exchange Rates` page and FX request quote preview.
4. Update docs and verify backend/frontend builds.
