#!/usr/bin/env bash
# Recover failed Prisma migrations on production (partial apply, owner errors, etc.).
#
# Usage (after setting MIGRATE_DATABASE_URL in .env.production):
#   set -a && source .env.production && set +a
#   ./scripts/prod-migrate-recover.sh [failed_migration_name]
#
# With Cloud SQL Auth Proxy:
#   ./scripts/prod-migrate-recover-via-proxy.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FAILED="${1:-20250606120000_dealer_restock_password_reset}"

if [[ -z "${MIGRATE_DATABASE_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
  if [[ -f .env.production ]]; then
    # shellcheck disable=SC1091
    set -a && source .env.production && set +a
  fi
fi

MIGRATE_URL="${MIGRATE_DATABASE_URL:-}"
if [[ -z "$MIGRATE_URL" ]]; then
  echo "Set MIGRATE_DATABASE_URL in .env.production to the postgres superuser URL."
  exit 1
fi

run_sql() {
  DATABASE_URL="$MIGRATE_URL" npx prisma db execute --file "$1"
}

FIX_SQL="$ROOT/prisma/sql/fix_${FAILED}.sql"
if [[ -f "$FIX_SQL" ]]; then
  echo "1/3 Apply idempotent fix SQL for $FAILED"
  run_sql "$FIX_SQL"
else
  echo "1/3 No fix SQL for $FAILED (skipping)"
fi

echo "2/3 Mark migration as applied: $FAILED"
if [[ -f "$FIX_SQL" ]]; then
  DATABASE_URL="$MIGRATE_URL" npx prisma migrate resolve --applied "$FAILED"
else
  echo "   No fix SQL — mark rolled back and redeploy..."
  DATABASE_URL="$MIGRATE_URL" npx prisma migrate resolve --rolled-back "$FAILED"
fi

echo "3/3 Deploy remaining migrations..."
DATABASE_URL="$MIGRATE_URL" npx prisma migrate deploy

echo ""
echo "Migrations OK. Continue with:"
echo "  ./scripts/prod-db-setup-via-proxy.sh"
