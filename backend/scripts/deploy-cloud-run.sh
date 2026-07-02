#!/usr/bin/env bash
# Deploy Mart API to Google Cloud Run (production).
#
# Prerequisites:
#   - gcloud CLI logged in: gcloud auth login
#   - APIs enabled: run.googleapis.com, cloudbuild.googleapis.com, artifactregistry.googleapis.com
#   - DATABASE_URL for production Postgres (Cloud SQL or other) available below or in .env.production
#
# Usage:
#   export GCP_PROJECT="your-gcp-project-id"
#   export GCP_REGION="asia-south1"
#   export GCP_SERVICE="mart-api"
#   # Production DB (required at runtime):
#   export DATABASE_URL="postgresql://USER:PASS@/mart?host=/cloudsql/PROJECT:REGION:INSTANCE"
#   export JWT_SECRET="your-long-secret"
#   # Optional Razorpay test keys:
#   export RAZORPAY_KEY_ID="rzp_test_..."
#   export RAZORPAY_KEY_SECRET="..."
#   ./scripts/sync-gcp-secrets.sh   # store Razorpay in Secret Manager (recommended)
#
#   ./scripts/deploy-cloud-run.sh

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
GCP_REGION="${GCP_REGION:-asia-south1}"
GCP_SERVICE="${GCP_SERVICE:-mart-api}"
CLOUDSQL_INSTANCE="${CLOUDSQL_INSTANCE:-}"
USE_GCP_SECRETS="${USE_GCP_SECRETS:-true}"
JWT_SECRET="${JWT_SECRET:-}"
DATABASE_URL="${DATABASE_URL:-}"

if [[ -z "$DATABASE_URL" && "$USE_GCP_SECRETS" != "true" ]]; then
  echo "Set DATABASE_URL to your production Postgres connection string."
  exit 1
fi

if [[ -z "$JWT_SECRET" && "$USE_GCP_SECRETS" != "true" ]]; then
  echo "Set JWT_SECRET for production auth."
  exit 1
fi

if [[ -z "$CLOUDSQL_INSTANCE" && "$GCP_PROJECT" == "knsr-mart-9789e4" ]]; then
  CLOUDSQL_INSTANCE="knsr-mart-9789e4:asia-south1:mart-pg"
fi

echo "Project: $GCP_PROJECT  Region: $GCP_REGION  Service: $GCP_SERVICE"
if [[ -n "$CLOUDSQL_INSTANCE" ]]; then
  echo "Cloud SQL: $CLOUDSQL_INSTANCE"
fi

gcloud config set project "$GCP_PROJECT"

gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com \
  --project="$GCP_PROJECT" 2>/dev/null || true

# gcloud --env-vars-file requires every value to be a YAML string (e.g. SMTP_PORT must be "587", not 587).
yaml_env_line() {
  local key="$1"
  local value="$2"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  printf '%s: "%s"\n' "$key" "$value"
}

ENV_FILE="$(mktemp)"
trap 'rm -f "$ENV_FILE"' EXIT
{
  yaml_env_line NODE_ENV production
  yaml_env_line JWT_EXPIRES_IN "${JWT_EXPIRES_IN:-7d}"
  # Push notifications (FCM HTTP v1). Defaults to the deploy project, which is also
  # the linked Firebase project, so the Cloud Run service account can send via ADC.
  yaml_env_line FIREBASE_PROJECT_ID "${FIREBASE_PROJECT_ID:-$GCP_PROJECT}"
  if [[ "$USE_GCP_SECRETS" != "true" ]]; then
    yaml_env_line JWT_SECRET "$JWT_SECRET"
    yaml_env_line DATABASE_URL "$DATABASE_URL"
    [[ -n "${RAZORPAY_KEY_ID:-}" ]] && yaml_env_line RAZORPAY_KEY_ID "$RAZORPAY_KEY_ID"
    [[ -n "${RAZORPAY_KEY_SECRET:-}" ]] && yaml_env_line RAZORPAY_KEY_SECRET "$RAZORPAY_KEY_SECRET"
    [[ -n "${RAZORPAY_WEBHOOK_SECRET:-}" ]] && yaml_env_line RAZORPAY_WEBHOOK_SECRET "$RAZORPAY_WEBHOOK_SECRET"
    [[ -n "${TWILIO_AUTH_TOKEN:-}" ]] && yaml_env_line TWILIO_AUTH_TOKEN "$TWILIO_AUTH_TOKEN"
  fi
  if [[ -z "${BREVO_API_KEY:-}" && -z "${SMTP_HOST:-}" ]]; then
    echo "WARNING: No BREVO_API_KEY or SMTP_HOST — onboarding emails will not send."
  fi
  if [[ -z "${TWILIO_ACCOUNT_SID:-}" || -z "${TWILIO_AUTH_TOKEN:-}" ]]; then
    echo "WARNING: TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN missing — registration OTP will not send."
  elif [[ -z "${TWILIO_VERIFY_SERVICE_SID:-}" && -z "${TWILIO_MESSAGING_SERVICE_SID:-}" && -z "${TWILIO_SMS_FROM:-}" ]]; then
    echo "WARNING: Set TWILIO_VERIFY_SERVICE_SID (VA…) or TWILIO_MESSAGING_SERVICE_SID / TWILIO_SMS_FROM for OTP."
  fi
  [[ -n "${SMTP_HOST:-}" ]] && yaml_env_line SMTP_HOST "$SMTP_HOST"
  [[ -n "${SMTP_PORT:-}" ]] && yaml_env_line SMTP_PORT "$SMTP_PORT"
  [[ -n "${SMTP_SECURE:-}" ]] && yaml_env_line SMTP_SECURE "$SMTP_SECURE"
  [[ -n "${SMTP_USER:-}" ]] && yaml_env_line SMTP_USER "$SMTP_USER"
  [[ -n "${SMTP_PASS:-}" ]] && yaml_env_line SMTP_PASS "$SMTP_PASS"
  [[ -n "${BREVO_API_KEY:-}" ]] && yaml_env_line BREVO_API_KEY "$BREVO_API_KEY"
  [[ -n "${BREVO_SMTP_LOGIN:-}" ]] && yaml_env_line BREVO_SMTP_LOGIN "$BREVO_SMTP_LOGIN"
  [[ -n "${MAIL_FROM:-}" ]] && yaml_env_line MAIL_FROM "$MAIL_FROM"
  [[ -n "${MAIL_APP_NAME:-}" ]] && yaml_env_line MAIL_APP_NAME "$MAIL_APP_NAME"
  [[ -n "${TWILIO_ACCOUNT_SID:-}" ]] && yaml_env_line TWILIO_ACCOUNT_SID "$TWILIO_ACCOUNT_SID"
  [[ -n "${TWILIO_VERIFY_SERVICE_SID:-}" ]] && yaml_env_line TWILIO_VERIFY_SERVICE_SID "$TWILIO_VERIFY_SERVICE_SID"
  [[ -n "${TWILIO_MESSAGING_SERVICE_SID:-}" ]] && yaml_env_line TWILIO_MESSAGING_SERVICE_SID "$TWILIO_MESSAGING_SERVICE_SID"
  [[ -n "${TWILIO_SMS_FROM:-}" ]] && yaml_env_line TWILIO_SMS_FROM "$TWILIO_SMS_FROM"
  [[ -n "${TWILIO_SMS_OTP_MESSAGE:-}" ]] && yaml_env_line TWILIO_SMS_OTP_MESSAGE "$TWILIO_SMS_OTP_MESSAGE"
  [[ -n "${SMS_OTP_EXPIRY_MINUTES:-}" ]] && yaml_env_line SMS_OTP_EXPIRY_MINUTES "$SMS_OTP_EXPIRY_MINUTES"
} > "$ENV_FILE"

DEPLOY_ARGS=(
  --source .
  --region "$GCP_REGION"
  --platform managed
  --allow-unauthenticated
  --port 8080
  --memory 512Mi
  --cpu 1
  --min-instances 0
  --max-instances 10
  --env-vars-file "$ENV_FILE"
  --project "$GCP_PROJECT"
)

if [[ -n "$CLOUDSQL_INSTANCE" ]]; then
  DEPLOY_ARGS+=(--add-cloudsql-instances="$CLOUDSQL_INSTANCE")
fi

if [[ "$USE_GCP_SECRETS" == "true" ]]; then
  SECRET_BINDINGS="DATABASE_URL=MART_DATABASE_URL:latest,JWT_SECRET=MART_JWT_SECRET:latest"
  if gcloud secrets describe MART_RAZORPAY_KEY_ID --project="$GCP_PROJECT" &>/dev/null; then
    SECRET_BINDINGS+=",RAZORPAY_KEY_ID=MART_RAZORPAY_KEY_ID:latest"
  fi
  if gcloud secrets describe MART_RAZORPAY_KEY_SECRET --project="$GCP_PROJECT" &>/dev/null; then
    SECRET_BINDINGS+=",RAZORPAY_KEY_SECRET=MART_RAZORPAY_KEY_SECRET:latest"
  fi
  if gcloud secrets describe MART_RAZORPAY_WEBHOOK_SECRET --project="$GCP_PROJECT" &>/dev/null; then
    SECRET_BINDINGS+=",RAZORPAY_WEBHOOK_SECRET=MART_RAZORPAY_WEBHOOK_SECRET:latest"
  fi
  if gcloud secrets describe MART_TWILIO_AUTH_TOKEN --project="$GCP_PROJECT" &>/dev/null; then
    SECRET_BINDINGS+=",TWILIO_AUTH_TOKEN=MART_TWILIO_AUTH_TOKEN:latest"
  fi
  DEPLOY_ARGS+=(--set-secrets="$SECRET_BINDINGS")
fi

echo "Deploying from source (Cloud Build)..."
gcloud run deploy "$GCP_SERVICE" "${DEPLOY_ARGS[@]}" --quiet

URL="$(gcloud run services describe "$GCP_SERVICE" --region "$GCP_REGION" --format='value(status.url)' --project "$GCP_PROJECT")"
echo ""
echo "Deployed: $URL"
echo "Update Mart/android/local.properties if the URL changed:"
echo "  mart.api.base.url.release=$URL"
