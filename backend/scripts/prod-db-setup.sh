#!/usr/bin/env bash
# Run migrations + import Excel catalog against PRODUCTION database.
# DATABASE_URL must point at Cloud SQL / production Postgres (not localhost:5435).
#
# Usage:
#   export DATABASE_URL="postgresql://..."
#   ./scripts/prod-db-setup.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -z "${DATABASE_URL:-}" ]]; then
  if [[ -f .env.production ]]; then
    # shellcheck disable=SC1091
    set -a && source .env.production && set +a
  fi
fi

if [[ -z "${DATABASE_URL:-}" ]]; then
  echo "Export DATABASE_URL to your production Postgres before running this script."
  exit 1
fi

if echo "$DATABASE_URL" | grep -q 'localhost:5435'; then
  echo "WARNING: DATABASE_URL looks like local Docker. Use production Cloud SQL URL instead."
  read -r -p "Continue anyway? [y/N] " ans
  [[ "$ans" == [yY]* ]] || exit 1
fi

MIGRATE_URL="${MIGRATE_DATABASE_URL:-$DATABASE_URL}"

echo "1/3 Prisma migrate deploy..."
if [[ -n "${MIGRATE_DATABASE_URL:-}" ]]; then
  echo "  Using MIGRATE_DATABASE_URL (postgres owner required for DDL)."
  DATABASE_URL="$MIGRATE_URL" npx prisma migrate deploy
else
  echo "  Tip: set MIGRATE_DATABASE_URL to postgres if you see 'must be owner of table'."
  npx prisma migrate deploy
fi

echo "2/3 Repair bulk-shipping columns (idempotent)..."
if [[ -n "${MIGRATE_DATABASE_URL:-}" ]]; then
  DATABASE_URL="$MIGRATE_URL" npm run db:repair-product-bulk-shipping
else
  npm run db:repair-product-bulk-shipping
fi

echo "3/4 Import products from Mart/product_upload_template.xls..."
npm run products:replace-from-template-xls

echo "4/4 Ensure dealer stock (import clears stock rows)..."
npm run db:seed-dealer-stock

echo "Done. Re-import finished — check script output for total product count."
echo ""
echo "Tip: create admin once (safe — does not wipe products):"
echo "  ./scripts/prod-seed-admin-via-proxy.sh"
