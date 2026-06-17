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

**Same machine**, with `DATABASE_URL` pointing at **production** (not Docker 5435):

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
