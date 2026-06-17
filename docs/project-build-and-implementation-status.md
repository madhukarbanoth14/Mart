# KNSR Mart App - Build and Technical Implementation

## 1) What We Are Building

KNSR Mart is a role-based retail distribution platform for FMCG operations, built for investor demo and early field validation.

The app supports the full flow across:

- **Shopkeepers**: browse catalog, place orders, complete mock payment, view invoice, track delivery.
- **Dealers**: manage incoming orders, confirm fulfillment, view stock, mark delivery.
- **Admin / Employee**: oversee users, onboarding, product catalog, areas, and operations dashboards.

The current product direction is to keep the backend as the source of truth for pricing, discount, GST, order state, and role access, while the Android app provides a polished role-first experience.

## 2) Technical Implementation (Current)

### Backend (`Mart/backend`)

- **Framework**: NestJS (TypeScript)
- **Database ORM**: Prisma
- **Database**: PostgreSQL
- **Authentication**: JWT + Passport
- **Authorization**: RBAC (role guards)
- **Core modules**:
  - `AuthModule`
  - `UsersModule`
  - `ProductsModule`
  - `AreasModule`
  - `DealerAssignmentsModule`
  - `OrdersModule`
  - `StockModule`
  - `InvoicesModule`

### Data Model Highlights

Implemented core entities include:

- `Company`
- `User` (roles: `ADMIN`, `EMPLOYEE`, `DEALER`, `SHOPKEEPER`)
- `Area` (dealer assignment)
- `Product` (GST and role discount fields)
- `Stock` (dealer-product inventory)
- `Order` + `OrderItem` (server-calculated totals)
- `Invoice` (generated for confirmed orders)

### Android App (`Mart/android`)

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: ViewModel + state-driven UI
- **Networking**: Retrofit + OkHttp + Gson
- **Session persistence**: DataStore-backed session repository
- **Navigation**: role-based graph routing (shopkeeper/dealer/admin-employee)
- **Build config toggles**:
  - `API_BASE_URL`
  - `USE_LOCAL_DEMO_AUTH`
  - `DEMO_MODE` (simulated investor flow support)

### API Integration Coverage

Android is wired to backend APIs for:

- Auth: login + current user
- Products: list/create/update/delete
- Orders: list/create/confirm/deliver/mock payment
- Invoices: by-order + list
- Stock: list
- Users: list + dealer/shopkeeper creation
- Areas: list

## 3) Core Features

### A) Authentication and Role Routing

- JWT-based login is implemented.
- Session persistence and splash-based routing are implemented.
- Role-based home navigation is implemented for all 4 roles.

### B) Catalog and SKU Management

- Product listing is available across roles.
- Admin SKU create/update/delete is implemented in backend and wired in Android.
- Shelf/category-oriented browsing exists for shopkeeper UX.

### C) Order Lifecycle

- Shopkeeper cart and order creation are implemented.
- Backend computes discount, GST, and final totals.
- Dealer/admin/employee confirmation flow is implemented.
- Delivery marking endpoint is implemented and consumed.

### D) Payment (Demo)

- Mock payment endpoint exists and is integrated.
- Android supports payment action from order detail.

### E) Invoicing

- Invoice retrieval by order is implemented.
- Android invoice screen is implemented.
- PDF generation and share flow is implemented in Android.

### F) Tracking

- Tracking timeline UI is implemented.
- API status is combined with demo simulation for investor narrative.

### G) Ops / Governance

- Users list and onboarding endpoints are implemented.
- Area and dealer assignment model is implemented.
- Stock visibility for operations is implemented.

## 4) Implementation Stage (Current Snapshot)

### Stage 1 - Platform foundation: **Completed**

- Backend modules, auth, RBAC, Prisma schema, migrations, and seed data are in place.
- Android app shell, navigation, session, networking, and app container are in place.

### Stage 2 - End-to-end business flow: **Completed (Demo Scope)**

- Shopkeeper ordering flow works.
- Dealer confirmation + stock interaction works.
- Mock payment, invoice view/PDF, and tracking timeline are implemented.

### Stage 3 - Demo readiness: **Completed**

- Demo users and sample catalog are seeded.
- Investor demo mode and local demo auth options are available.
- Role dashboards and role-specific journeys are in place.

### Stage 4 - Production hardening: **In Progress / Pending**

Remaining non-demo work includes:

- Production-grade security hardening and operational controls.
- Real payment gateway integration.
- CI/CD and deployment automation.
- Comprehensive automated tests (unit/integration/e2e) expansion.
- Monitoring, analytics, and stronger error observability.

## 5) Demo Data and Accounts

Demo credentials and dashboard mapping are documented in:

- `Mart/docs/demo-accounts-and-dashboards.md`

Database troubleshooting and setup notes are documented in:

- `Mart/docs/database-troubleshooting.md`

## 6) Recommended Next Technical Milestones

1. **Quality and testing pass**
   - Expand backend unit/integration tests for order pricing, role guard coverage, and invoice generation.
   - Add Android UI/state tests for role routing and critical flows.

2. **Production readiness lane**
   - Introduce environment-specific configuration and secure secret handling.
   - Harden authentication/session lifecycle and API error contracts.

3. **Payments and operations**
   - Replace mock payment with a real provider integration path.
   - Add richer order filters, operational analytics, and auditability.

4. **Release engineering**
   - Set up CI checks (build, lint, test, migration validation).
   - Prepare release signing and deployment pipeline.

---

This document reflects the current implemented state in the repository and positions the app as **demo-complete, production-hardening pending**.
