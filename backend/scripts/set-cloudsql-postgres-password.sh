#!/usr/bin/env bash
# Set Cloud SQL postgres password (use straight quotes — not ‘curly’ quotes).
#
# Usage:
#   ./scripts/set-cloudsql-postgres-password.sh
#   ./scripts/set-cloudsql-postgres-password.sh 'YourPassword'

set -euo pipefail

PROJECT="${GCP_PROJECT:-knsr-mart-9789e4}"
INSTANCE="${CLOUDSQL_INSTANCE_NAME:-mart-pg}"

if [[ $# -ge 1 ]]; then
  PW="$1"
else
  read -r -s -p "New postgres password: " PW
  echo
fi

if [[ -z "$PW" ]]; then
  echo "Password cannot be empty."
  exit 1
fi

# Reject common curly-quote copy/paste mistakes.
if [[ "$PW" == *"‘"* || "$PW" == *"’"* || "$PW" == *"“"* || "$PW" == *"”"* ]]; then
  echo "Password contains curly/smart quotes. Use plain ASCII characters only."
  exit 1
fi

echo "Updating postgres password on ${INSTANCE}..."
gcloud sql users set-password postgres \
  --instance="$INSTANCE" \
  --project="$PROJECT" \
  --password="$PW"

ENC_PW="$(python3 -c "import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1], safe=''))" "$PW")"
echo ""
echo "Done. Update .env.production MIGRATE_DATABASE_URL, for example:"
echo "  MIGRATE_DATABASE_URL=\"postgresql://postgres:${ENC_PW}@34.93.71.44:5432/mart?schema=public\""
echo ""
echo "Then:"
echo "  set -a && source .env.production && set +a"
echo "  ./scripts/prod-migrate-recover-via-proxy.sh"
