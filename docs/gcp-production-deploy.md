# GCP production deploy (Mart API + product catalog)

The release APK calls **Cloud Run** by default:

`https://mart-api-95628498734.asia-south1.run.app`

Local imports (`npm run products:replace-from-template-xls` on `localhost:5435`) do **not** update production until you run the same steps against the **production** `DATABASE_URL`.

## 1. Prerequisites

- `gcloud` CLI installed and logged in: `gcloud auth login`
- GCP project that owns the `mart-api` Cloud Run service
- Production Postgres URL (`DATABASE_URL`) — usually **Cloud SQL**
- `JWT_SECRET` (long random string)
- Optional: Razorpay **test** keys for payments

## 2. Configure production env (do not commit secrets)

Create `Mart/backend/.env.production` (gitignored) on your machine:

```bash
DATABASE_URL="postgresql://USER:PASSWORD@HOST:5432/mart?schema=public"
JWT_SECRET="change-to-a-long-random-secret"
JWT_EXPIRES_IN=7d
RAZORPAY_KEY_ID=rzp_test_xxxx
RAZORPAY_KEY_SECRET=xxxx
```

Sync Razorpay into **Secret Manager** (recommended — not plain env vars on Cloud Run):

```bash
cd Mart/backend
set -a && source .env.production && set +a
./scripts/sync-gcp-secrets.sh
```

Then deploy:

```bash
./scripts/deploy-cloud-run.sh
```

For **Cloud SQL** from Cloud Run, Google often uses the Unix socket form:

```text
postgresql://USER:PASSWORD@/mart?host=/cloudsql/PROJECT:REGION:INSTANCE
```

and attach the Cloud SQL instance to the Cloud Run service (`--add-cloudsql-instances`).

## 3. Deploy API to Cloud Run

```bash
cd Mart/backend
chmod +x scripts/deploy-cloud-run.sh scripts/prod-db-setup.sh

export GCP_PROJECT="YOUR_PROJECT_ID"    # e.g. project that matches mart-api URL
export GCP_REGION="asia-south1"
export GCP_SERVICE="mart-api"

# Load secrets from .env.production
set -a && source .env.production && set +a

./scripts/deploy-cloud-run.sh
```

Note the printed **service URL**. If it differs from the APK default, set in `Mart/android/local.properties`:

```properties
mart.api.base.url.release=https://YOUR-SERVICE-URL
```

Then rebuild the release APK.

## 4. Migrate DB and load 185 products (production)

**Same machine**, with `DATABASE_URL` pointing at **production** (not Docker 5435).

### Option A — Cloud SQL Auth Proxy (recommended)

No IP allowlist changes. Uses your gcloud login.

```bash
brew install cloud-sql-proxy   # once
gcloud auth application-default login   # once

cd Mart/backend
set -a && source .env.production && set +a
./scripts/prod-db-setup-via-proxy.sh
```

**Important:** In `.env.production`, set two URLs:

```env
DATABASE_URL="postgresql://APP_USER:...@HOST:5432/mart?schema=public"
MIGRATE_DATABASE_URL="postgresql://postgres:...@HOST:5432/mart?schema=public"
```

Migrations must run as **postgres** (table owner). Using only `APP_USER` causes `must be owner of table User`.

If a migration failed (P3018), recover then re-run setup:

```bash
./scripts/prod-migrate-recover-via-proxy.sh
./scripts/prod-db-setup-via-proxy.sh
```

### Option B — Direct public IP (must allowlist your Mac)

Cloud SQL `mart-pg` only accepts IPs in **Authorized networks**. If you see `P1001` at `34.93.71.44:5432`:

```bash
cd Mart/backend
./scripts/authorize-my-ip-cloudsql.sh
set -a && source .env.production && set +a
./scripts/prod-db-setup.sh
```

### Option C — manual

```bash
cd Mart/backend
set -a && source .env.production && set +a
./scripts/prod-db-setup.sh
```

This runs:

1. `prisma migrate deploy`
2. `npm run db:repair-product-bulk-shipping`
3. `npm run products:replace-from-template-xls` → **185 products** from `Mart/product_upload_template.xls`

## 5. Verify

- Open the app (release APK), sign in as shopkeeper
- **Home**: scroll — no HTTP 404; products load
- **Images**: need valid `imageUrl` in the Excel sheet (direct `https://` image links)

## 6. Find your GCP project

If `mart-api` is not in `clean-assertz-platform`:

```bash
gcloud projects list
gcloud run services list --project=PROJECT_ID --region=asia-south1
```

Use the project where `mart-api` appears.

## 7. Common issues

| Issue | Fix |
|--------|-----|
| `bulkShippingMinQty` column missing | `npm run db:repair-product-bulk-shipping` on **production** DB |
| App shows HTTP 404 on scroll | Redeploy latest backend; app now uses `GET /products` fallback |
| No images | Fill `imageUrl` in Excel; re-run `products:replace-from-template-xls` on prod DB |
| Login 500 | Wrong `DATABASE_URL` on Cloud Run — check Cloud Run env vars / logs |
| `P1001` at `34.93.71.44:5432` on `prod-db-setup` | Your Mac IP is not in Cloud SQL authorized networks — use `./scripts/prod-db-setup-via-proxy.sh` or `./scripts/authorize-my-ip-cloudsql.sh` |
| `must be owner of table User` (P3018) | Set `MIGRATE_DATABASE_URL` to **postgres** user; run `./scripts/prod-migrate-recover-via-proxy.sh` then `./scripts/prod-db-setup-via-proxy.sh` |
