#!/usr/bin/env bash
# Build signed Android App Bundle (AAB) for Google Play Console upload.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

KEYSTORE="${ROOT}/flashmart-upload.jks"
PROPS="${ROOT}/keystore.properties"

if [[ ! -f "$PROPS" || ! -f "$KEYSTORE" ]]; then
  echo "Play upload keystore not found."
  echo "Run once: ./scripts/setup-play-upload-keystore.sh"
  exit 1
fi

if grep -q 'mart.use.local.demo.auth=true' local.properties 2>/dev/null; then
  echo "ERROR: mart.use.local.demo.auth=true in local.properties — disable for Play Store."
  exit 1
fi

chmod +x gradlew
./gradlew bundleRelease

AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"
OUT="$ROOT/../FlashMart-release.aab"
cp -f "$AAB" "$OUT"

echo ""
echo "Play Store bundle: $OUT"
echo ""
echo "Upload in Play Console:"
echo "  Testing → Internal testing → Create release → Upload AAB"
echo "  (or Production when ready)"
echo ""
jarsigner -verify -verbose -certs "$OUT" 2>/dev/null | head -3 || true
