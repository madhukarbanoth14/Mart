#!/usr/bin/env bash
# Recover failed migrations via Cloud SQL Auth Proxy (uses MIGRATE_DATABASE_URL).
#
# Usage:
#   set -a && source .env.production && set +a
#   ./scripts/prod-migrate-recover-via-proxy.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck disable=SC1091
source "$ROOT/scripts/lib/cloud-sql-proxy.sh"

INSTANCE="${CLOUDSQL_INSTANCE:-knsr-mart-9789e4:asia-south1:mart-pg}"
PROXY_PORT="${CLOUDSQL_PROXY_PORT:-5433}"

if [[ -z "${MIGRATE_DATABASE_URL:-}" ]]; then
  if [[ -f .env.production ]]; then
    # shellcheck disable=SC1091
    set -a && source .env.production && set +a
  fi
fi

if [[ -z "${MIGRATE_DATABASE_URL:-}" ]]; then
  echo "Set MIGRATE_DATABASE_URL (postgres user) in .env.production first."
  exit 1
fi

if ! command -v cloud-sql-proxy >/dev/null 2>&1; then
  echo "cloud-sql-proxy not found.  brew install cloud-sql-proxy"
  exit 1
fi

export MIGRATE_DATABASE_URL="$(rewrite_proxy_database_url "$MIGRATE_DATABASE_URL" "$PROXY_PORT")"

start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

# Do NOT `exec` here: exec replaces this process and discards the EXIT trap, so
# the proxy we started would linger (and keep a downstream pipe open). Run it as
# a child instead so the trap fires and the proxy is stopped on exit.
./scripts/prod-migrate-recover.sh "$@"
