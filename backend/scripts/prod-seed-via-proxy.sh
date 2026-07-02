#!/usr/bin/env bash
# Seed admin + catalog on production via Cloud SQL Auth Proxy.
# WARNING: full seed replaces products/brands with prisma/seed.ts defaults.
# After this, re-import 185 products if needed:
#   npm run products:replace-from-template-xls
#
# Usage:
#   set -a && source .env.production && set +a
#   ./scripts/prod-seed-via-proxy.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck disable=SC1091
source "$ROOT/scripts/lib/cloud-sql-proxy.sh"

INSTANCE="${CLOUDSQL_INSTANCE:-knsr-mart-9789e4:asia-south1:mart-pg}"
PROXY_PORT="${CLOUDSQL_PROXY_PORT:-5433}"

if [[ -z "${DATABASE_URL:-}" ]]; then
  # shellcheck disable=SC1091
  [[ -f .env.production ]] && set -a && source .env.production && set +a
fi

if [[ -z "${DATABASE_URL:-}" ]]; then
  echo "Set DATABASE_URL in .env.production"
  exit 1
fi

if [[ -z "${SEED_ADMIN_PASSWORD:-}" ]]; then
  echo "Set SEED_ADMIN_EMAIL and SEED_ADMIN_PASSWORD in .env.production"
  exit 1
fi

export DATABASE_URL="$(rewrite_proxy_database_url "$DATABASE_URL" "$PROXY_PORT")"
start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

echo "Seeding production DB via proxy (admin: ${SEED_ADMIN_EMAIL:-madhukar@techfylabs.com})..."
npm run prisma:seed

echo ""
echo "Verify admin exists:"
DATABASE_URL="$DATABASE_URL" npx prisma db execute --stdin <<'SQL'
SELECT email, role, status FROM "User" WHERE role = 'ADMIN' ORDER BY email;
SQL
