#!/usr/bin/env bash
# Test MIGRATE_DATABASE_URL through Cloud SQL Auth Proxy (no migrate changes).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck disable=SC1091
source "$ROOT/scripts/lib/cloud-sql-proxy.sh"

INSTANCE="${CLOUDSQL_INSTANCE:-knsr-mart-9789e4:asia-south1:mart-pg}"
PROXY_PORT="${CLOUDSQL_PROXY_PORT:-5433}"

if [[ -z "${MIGRATE_DATABASE_URL:-}" ]]; then
  # shellcheck disable=SC1091
  [[ -f .env.production ]] && set -a && source .env.production && set +a
fi

if [[ -z "${MIGRATE_DATABASE_URL:-}" ]]; then
  echo "Set MIGRATE_DATABASE_URL in .env.production"
  exit 1
fi

export MIGRATE_DATABASE_URL="$(rewrite_proxy_database_url "$MIGRATE_DATABASE_URL" "$PROXY_PORT")"
start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

echo "Testing connection..."
if DATABASE_URL="$MIGRATE_DATABASE_URL" npx prisma db execute --stdin <<'SQL'
SELECT current_user, current_database();
SQL
then
  echo "OK — postgres credentials work via proxy."
else
  echo "FAILED — fix postgres password (see scripts/set-cloudsql-postgres-password.sh)."
  exit 1
fi
