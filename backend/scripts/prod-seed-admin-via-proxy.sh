#!/usr/bin/env bash
# Create/update production admin ONLY (does not wipe products). Uses Cloud SQL Auth Proxy.
#
# Usage:
#   set -a && source .env.production && set +a
#   ./scripts/prod-seed-admin-via-proxy.sh

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

if [[ -z "${DATABASE_URL:-}" || -z "${SEED_ADMIN_PASSWORD:-}" ]]; then
  echo "Set DATABASE_URL, SEED_ADMIN_EMAIL, SEED_ADMIN_PASSWORD in .env.production"
  exit 1
fi

export DATABASE_URL="$(rewrite_proxy_database_url "$DATABASE_URL" "$PROXY_PORT")"
start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

echo "Creating/updating admin via proxy (${SEED_ADMIN_EMAIL:-madhukar@techfylabs.com})..."
npx ts-node --project prisma/tsconfig.seed.json prisma/seed-admin-only.ts

echo ""
echo "Done. Sign in on the app with SEED_ADMIN_EMAIL / SEED_ADMIN_PASSWORD."
echo "If Products still empty, re-run this script (links admin to demo-company catalog)."
