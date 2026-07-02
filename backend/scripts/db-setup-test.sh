#!/usr/bin/env bash
# Create / migrate / seed the local TEST database (mart_test). Never touches production.
#
# Usage (from Mart/backend):
#   npm run db:setup:test

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

export DATABASE_URL="${DATABASE_URL:-postgresql://postgres:postgres@localhost:5436/mart_test?schema=public}"

echo "Starting test Postgres (mart_test on localhost:5436)..."
if docker info >/dev/null 2>&1; then
  npm run db:up:test
  TEST_PORT=5436
  export DATABASE_URL="${DATABASE_URL:-postgresql://postgres:postgres@localhost:5436/mart_test?schema=public}"
else
  echo "Docker is not running — using Homebrew Postgres on localhost:5432 instead."
  echo "  (Start Docker Desktop and re-run if you prefer the isolated test container.)"
  if ! pg_isready -h localhost -p 5432 -q 2>/dev/null; then
    echo "No Postgres on port 5432. Start Docker Desktop or install/start local Postgres."
    exit 1
  fi
  createdb mart_test 2>/dev/null || true
  TEST_PORT=5432
  export DATABASE_URL="${DATABASE_URL:-postgresql://$(whoami)@localhost:5432/mart_test?schema=public}"
fi

echo "Waiting for Postgres..."
for _ in $(seq 1 30); do
  if (echo >/dev/tcp/127.0.0.1/"$TEST_PORT") >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "Applying migrations to test DB..."
npx prisma migrate deploy

echo "Seeding test DB..."
npx prisma db seed

echo ""
echo "Test database ready."
echo "  DATABASE_URL=$DATABASE_URL"
echo "  Prisma Studio: npm run prisma:studio:test"
echo "  API:           npm run start:dev"
