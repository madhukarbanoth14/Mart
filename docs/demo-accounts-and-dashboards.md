# FlashMart — accounts and onboarding

## Database seed (production / fresh install)

Run `npm run prisma:seed` with these env vars set (see `.env.example`):

| Variable | Purpose |
|----------|---------|
| `SEED_ADMIN_EMAIL` | Single admin login email (default: `madhukar@techfylabs.com`) |
| `SEED_ADMIN_PASSWORD` | **Required** — min 8 characters |
| `SEED_ADMIN_NAME` | Display name for the admin user |
| `SEED_COMPANY_NAME` | Company name (default: FlashMart Distribution) |

The seed script:

- **Purges** legacy demo accounts (`@martdemo.com`, fixed `demo-user-*` IDs)
- Creates **one** `ADMIN` user (auto-generated UUID)
- Seeds territories (areas without dealers), brands, and products — **no** demo dealers, shopkeepers, or stock

Re-running seed is safe: existing admin email is reused; catalog is refreshed for that company.

## Onboarding flow (no seeded employees)

```
Admin (seed) → POST /users/employees → Employee logs in
  → Employee onboards dealer / shopkeeper (PENDING_APPROVAL)
  → Admin approves in Team tab → credentials emailed
```

Admin can also onboard dealers and shopkeepers directly (auto-approved when submitted by admin).

## Create employee

**In app (Admin):** Home → **Add employee**, or Team tab → **Add employee**

**API:** `POST /users/employees` — **ADMIN only**

```json
{
  "name": "Field Employee",
  "email": "employee@yourcompany.com",
  "phone": "9876543210",
  "password": "optional-min-8-chars"
}
```

If `password` is omitted, a secure password is generated and emailed (when SMTP is configured).

## Local offline demo (apps only)

Android/iOS can still use **local demo auth** when `mart.use.local.demo.auth=true` (debug builds). That mode does **not** write to the database.
