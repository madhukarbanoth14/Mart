#!/usr/bin/env bash
# Add your current public IP to Cloud SQL authorized networks (for direct DATABASE_URL access).
#
# Usage:
#   ./scripts/authorize-my-ip-cloudsql.sh

set -euo pipefail

PROJECT="${GCP_PROJECT:-knsr-mart-9789e4}"
INSTANCE="${CLOUDSQL_INSTANCE_NAME:-mart-pg}"
EXISTING_IP="${CLOUDSQL_EXISTING_IP:-43.241.123.37/32}"

MY_IP="$(curl -fsSL https://api.ipify.org)"
if [[ -z "$MY_IP" ]]; then
  echo "Could not detect your public IP."
  exit 1
fi

echo "Adding ${MY_IP}/32 to ${INSTANCE} (keeping ${EXISTING_IP})..."
gcloud sql instances patch "$INSTANCE" \
  --project="$PROJECT" \
  --authorized-networks="${EXISTING_IP},${MY_IP}/32" \
  --quiet

echo "Done. Re-run: ./scripts/prod-db-setup.sh"
