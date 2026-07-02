#!/usr/bin/env bash
# Apply pending Prisma migrations to production via Cloud SQL Auth Proxy.
# Does NOT import products or run seed scripts — migrations only.
#
# Prerequisites:
#   brew install cloud-sql-proxy
#   gcloud auth application-default login
#
# Usage (from Mart/backend):
#   set -a && source .env.production && set +a
#   ./scripts/prod-migrate-deploy-via-proxy.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck disable=SC1091
source "$ROOT/scripts/lib/cloud-sql-proxy.sh"

INSTANCE="${CLOUDSQL_INSTANCE:-knsr-mart-9789e4:asia-south1:mart-pg}"
PROXY_PORT="${CLOUDSQL_PROXY_PORT:-5433}"

if [[ -z "${MIGRATE_DATABASE_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
  if [[ -f .env.production ]]; then
    # shellcheck disable=SC1091
    set -a && source .env.production && set +a
  fi
fi

MIGRATE_URL="${MIGRATE_DATABASE_URL:-${DATABASE_URL:-}}"
if [[ -z "$MIGRATE_URL" ]]; then
  echo "Set MIGRATE_DATABASE_URL (postgres owner) in .env.production"
  exit 1
fi

if ! command -v cloud-sql-proxy >/dev/null 2>&1; then
  echo "cloud-sql-proxy not found. Install: brew install cloud-sql-proxy"
  exit 1
fi

export MIGRATE_DATABASE_URL="$(rewrite_proxy_database_url "$MIGRATE_URL" "$PROXY_PORT")"
echo "Prisma migrate deploy via 127.0.0.1:${PROXY_PORT} → ${INSTANCE}"

start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

DATABASE_URL="$MIGRATE_DATABASE_URL" npx prisma migrate deploy

echo "Done. Pending migrations applied."
