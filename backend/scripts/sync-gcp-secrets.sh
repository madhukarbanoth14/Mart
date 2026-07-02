#!/usr/bin/env bash
# Create or update Secret Manager entries used by Cloud Run (mart-api).
#
# Reads from .env.production (or env vars already exported):
#   RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET, RAZORPAY_WEBHOOK_SECRET (optional)
#   TWILIO_AUTH_TOKEN (optional — registration OTP SMS via Twilio)
#
# Usage:
#   cd Mart/backend
#   set -a && source .env.production && set +a
#   ./scripts/sync-gcp-secrets.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -f .env.production ]]; then
  # shellcheck disable=SC1091
  set -a && source .env.production && set +a
fi

GCP_PROJECT="${GCP_PROJECT:-}"
if [[ -z "$GCP_PROJECT" ]]; then
  GCP_PROJECT="$(gcloud config get-value project 2>/dev/null || true)"
fi
GCP_PROJECT="${GCP_PROJECT:-knsr-mart-9789e4}"
GCP_SERVICE="${GCP_SERVICE:-mart-api}"
GCP_REGION="${GCP_REGION:-asia-south1}"

gcloud config set project "$GCP_PROJECT" >/dev/null

gcloud services enable secretmanager.googleapis.com --project="$GCP_PROJECT" 2>/dev/null || true

PROJECT_NUMBER="$(gcloud projects describe "$GCP_PROJECT" --format='value(projectNumber)')"
RUNTIME_SA="$(gcloud run services describe "$GCP_SERVICE" \
  --region "$GCP_REGION" \
  --project "$GCP_PROJECT" \
  --format='value(spec.template.spec.serviceAccountName)' 2>/dev/null || true)"
RUNTIME_SA="${RUNTIME_SA:-${PROJECT_NUMBER}-compute@developer.gserviceaccount.com}"

upsert_secret() {
  local name="$1"
  local value="$2"
  if [[ -z "$value" ]]; then
    echo "Skip $name (empty value)"
    return 0
  fi
  if gcloud secrets describe "$name" --project="$GCP_PROJECT" &>/dev/null; then
    printf '%s' "$value" | gcloud secrets versions add "$name" --data-file=- --project="$GCP_PROJECT" >/dev/null
    echo "Updated secret: $name"
  else
    printf '%s' "$value" | gcloud secrets create "$name" \
      --data-file=- \
      --project="$GCP_PROJECT" \
      --replication-policy=automatic >/dev/null
    echo "Created secret: $name"
  fi
  gcloud secrets add-iam-policy-binding "$name" \
    --project="$GCP_PROJECT" \
    --member="serviceAccount:${RUNTIME_SA}" \
    --role="roles/secretmanager.secretAccessor" \
    --quiet >/dev/null
}

echo "Project: $GCP_PROJECT"
echo "Cloud Run runtime SA: $RUNTIME_SA"
echo ""

if [[ -z "${RAZORPAY_KEY_ID:-}" || -z "${RAZORPAY_KEY_SECRET:-}" ]]; then
  echo "Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in .env.production or the environment."
  exit 1
fi

upsert_secret MART_RAZORPAY_KEY_ID "$RAZORPAY_KEY_ID"
upsert_secret MART_RAZORPAY_KEY_SECRET "$RAZORPAY_KEY_SECRET"
upsert_secret MART_RAZORPAY_WEBHOOK_SECRET "${RAZORPAY_WEBHOOK_SECRET:-}"
upsert_secret MART_TWILIO_AUTH_TOKEN "${TWILIO_AUTH_TOKEN:-}"

echo ""
echo "Done. Redeploy Cloud Run so the service mounts the new secrets:"
echo "  ./scripts/deploy-cloud-run.sh"
