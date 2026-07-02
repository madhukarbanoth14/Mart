#!/usr/bin/env bash
# Build production release APK (Cloud Run API, sideload-ready).
# For Google Play, use ./scripts/build-release-aab.sh instead.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

chmod +x gradlew
./gradlew clean assembleRelease

APK="$ROOT/app/build/outputs/apk/release/app-release.apk"
OUT="$ROOT/../FlashMart-release.apk"
cp -f "$APK" "$OUT"
echo ""
echo "Release APK: $OUT"
echo "Install: adb install -r \"$OUT\""
