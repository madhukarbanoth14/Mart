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
#
#   ./scripts/deploy-cloud-run.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

GCP_PROJECT="${GCP_PROJECT:-}"
GCP_REGION="${GCP_REGION:-asia-south1}"
GCP_SERVICE="${GCP_SERVICE:-mart-api}"
CLOUDSQL_INSTANCE="${CLOUDSQL_INSTANCE:-}"
USE_GCP_SECRETS="${USE_GCP_SECRETS:-true}"
JWT_SECRET="${JWT_SECRET:-}"
DATABASE_URL="${DATABASE_URL:-}"

if [[ -z "$GCP_PROJECT" ]]; then
  echo "Set GCP_PROJECT to the project that hosts mart-api (Android release URL uses asia-south1)."
  echo "  export GCP_PROJECT=\"your-project-id\""
  exit 1
fi

if [[ -z "$DATABASE_URL" ]]; then
  if [[ -f .env.production ]]; then
    # shellcheck disable=SC1091
    set -a && source .env.production && set +a
    DATABASE_URL="${DATABASE_URL:-}"
    JWT_SECRET="${JWT_SECRET:-${JWT_SECRET}}"
  fi
fi

if [[ "$USE_GCP_SECRETS" != "true" ]]; then
  if [[ -z "$DATABASE_URL" ]]; then
    echo "Set DATABASE_URL to your production Postgres connection string."
    exit 1
  fi
  if [[ -z "$JWT_SECRET" ]]; then
    echo "Set JWT_SECRET for production auth."
    exit 1
  fi
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

ENV_FILE="$(mktemp)"
trap 'rm -f "$ENV_FILE"' EXIT
{
  echo "NODE_ENV: production"
  echo "JWT_EXPIRES_IN: ${JWT_EXPIRES_IN:-7d}"
  if [[ "$USE_GCP_SECRETS" != "true" ]]; then
    echo "JWT_SECRET: $JWT_SECRET"
    echo "DATABASE_URL: $DATABASE_URL"
  fi
  [[ -n "${RAZORPAY_KEY_ID:-}" ]] && echo "RAZORPAY_KEY_ID: $RAZORPAY_KEY_ID"
  [[ -n "${RAZORPAY_KEY_SECRET:-}" ]] && echo "RAZORPAY_KEY_SECRET: $RAZORPAY_KEY_SECRET"
  [[ -n "${RAZORPAY_WEBHOOK_SECRET:-}" ]] && echo "RAZORPAY_WEBHOOK_SECRET: $RAZORPAY_WEBHOOK_SECRET"
  [[ -n "${SMTP_HOST:-}" ]] && echo "SMTP_HOST: $SMTP_HOST"
  [[ -n "${SMTP_PORT:-}" ]] && echo "SMTP_PORT: $SMTP_PORT"
  [[ -n "${SMTP_SECURE:-}" ]] && echo "SMTP_SECURE: $SMTP_SECURE"
  [[ -n "${SMTP_USER:-}" ]] && echo "SMTP_USER: $SMTP_USER"
  [[ -n "${SMTP_PASS:-}" ]] && echo "SMTP_PASS: $SMTP_PASS"
  [[ -n "${MAIL_FROM:-}" ]] && echo "MAIL_FROM: $MAIL_FROM"
  [[ -n "${MAIL_APP_NAME:-}" ]] && echo "MAIL_APP_NAME: $MAIL_APP_NAME"
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
  DEPLOY_ARGS+=(
    --set-secrets="DATABASE_URL=MART_DATABASE_URL:latest,JWT_SECRET=MART_JWT_SECRET:latest"
  )
fi

echo "Deploying from source (Cloud Build)..."
gcloud run deploy "$GCP_SERVICE" "${DEPLOY_ARGS[@]}" --quiet

URL="$(gcloud run services describe "$GCP_SERVICE" --region "$GCP_REGION" --format='value(status.url)' --project "$GCP_PROJECT")"
echo ""
echo "Deployed: $URL"
echo "Update Mart/android/local.properties if the URL changed:"
echo "  mart.api.base.url.release=$URL"
