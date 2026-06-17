#!/usr/bin/env bash
# Build FlashMart for iOS Simulator (local API, no demo mode).
# Requires full Xcode (not Command Line Tools only).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! xcodebuild -version >/dev/null 2>&1; then
  echo "Install Xcode from the App Store, then run:"
  echo "  sudo xcode-select -s /Applications/Xcode.app/Contents/Developer"
  exit 1
fi

xcodegen generate
pod install

DERIVED="${TMPDIR:-/tmp}/Flashmart-DerivedData"
rm -rf "$DERIVED"
mkdir -p "$DERIVED"

xcodebuild \
  -workspace Flashmart.xcworkspace \
  -scheme Flashmart \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath "$DERIVED" \
  CODE_SIGNING_ALLOWED=NO \
  COMPILER_INDEX_STORE_ENABLE=NO \
  ONLY_ACTIVE_ARCH=YES \
  ARCHS=arm64 \
  EXCLUDED_ARCHS=x86_64 \
  build

APP="$DERIVED/Build/Products/Debug-iphonesimulator/Flashmart.app"
OUT="$ROOT/../FlashMart-simulator-local.zip"
rm -f "$OUT"
ditto -c -k --sequesterRsrc --keepParent "$APP" "$OUT"
echo ""
echo "Simulator build: $APP"
echo "Zip for sharing: $OUT"
echo ""
echo "Install on booted simulator:"
echo "  xcrun simctl install booted \"$APP\""
echo "  xcrun simctl launch booted com.knsrmart.flashmart"
