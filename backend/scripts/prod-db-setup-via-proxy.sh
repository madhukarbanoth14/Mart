#!/usr/bin/env bash
# Run prod-db-setup against Cloud SQL via Auth Proxy (no public IP allowlist needed).
#
# Prerequisites:
#   brew install cloud-sql-proxy
#   gcloud auth application-default login
#
# Usage (from Mart/backend):
#   set -a && source .env.production && set +a
#   ./scripts/prod-db-setup-via-proxy.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck disable=SC1091
source "$ROOT/scripts/lib/cloud-sql-proxy.sh"

INSTANCE="${CLOUDSQL_INSTANCE:-knsr-mart-9789e4:asia-south1:mart-pg}"
PROXY_PORT="${CLOUDSQL_PROXY_PORT:-5433}"

if [[ -z "${DATABASE_URL:-}" ]]; then
  if [[ -f .env.production ]]; then
    # shellcheck disable=SC1091
    set -a && source .env.production && set +a
  fi
fi

if [[ -z "${DATABASE_URL:-}" ]]; then
  echo "Export DATABASE_URL or create .env.production first."
  exit 1
fi

if ! command -v cloud-sql-proxy >/dev/null 2>&1; then
  echo "cloud-sql-proxy not found."
  echo "  brew install cloud-sql-proxy"
  echo "  gcloud auth application-default login"
  exit 1
fi

export DATABASE_URL="$(rewrite_proxy_database_url "$DATABASE_URL" "$PROXY_PORT")"
if [[ -n "${MIGRATE_DATABASE_URL:-}" ]]; then
  export MIGRATE_DATABASE_URL="$(rewrite_proxy_database_url "$MIGRATE_DATABASE_URL" "$PROXY_PORT")"
  echo "Using MIGRATE_DATABASE_URL via proxy (host 127.0.0.1:${PROXY_PORT})"
else
  echo "WARNING: MIGRATE_DATABASE_URL not set — migrations use DATABASE_URL."
  echo "  Add postgres owner URL to .env.production if migrate fails with 'must be owner'."
fi
echo "Using DATABASE_URL via proxy (host 127.0.0.1:${PROXY_PORT})"

start_cloud_sql_proxy_if_needed "$INSTANCE" "$PROXY_PORT"
trap 'stop_cloud_sql_proxy_if_started' EXIT

# Do NOT `exec` here: exec replaces this process and discards the EXIT trap, so
# the proxy we started would linger (and keep a downstream pipe open). Run it as
# a child instead so the trap fires and the proxy is stopped on exit.
./scripts/prod-db-setup.sh
