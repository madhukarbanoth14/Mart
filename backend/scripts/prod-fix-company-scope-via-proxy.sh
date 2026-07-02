#!/usr/bin/env bash
# Fix production users scoped to the wrong company (products live on demo-company).
# Safe to re-run. Does not delete products.
#
# Usage:
#   set -a && source .env.production && set +a
#   ./scripts/prod-fix-company-scope-via-proxy.sh

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

export DATABASE_URL="$(rewrite_proxy_database_url "$DATABASE_URL" "$PROXY_PORT")"
start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

echo "Realigning users to catalog company (demo-company)..."
npx ts-node --project prisma/tsconfig.seed.json prisma/seed-admin-only.ts

echo ""
echo "Done. Sign out and sign in again on the app, then open Products."
