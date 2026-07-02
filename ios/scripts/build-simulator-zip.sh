#!/usr/bin/env bash
# Build FlashMart for iOS Simulator against production Cloud Run API.
# Uses Release configuration (same API URL as FlashMart-release-ios / Android release APK).
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
  -configuration Release \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath "$DERIVED" \
  CODE_SIGNING_ALLOWED=NO \
  COMPILER_INDEX_STORE_ENABLE=NO \
  ONLY_ACTIVE_ARCH=YES \
  ARCHS=arm64 \
  EXCLUDED_ARCHS=x86_64 \
  build

APP="$DERIVED/Build/Products/Release-iphonesimulator/Flashmart.app"
OUT="$ROOT/../FlashMart-simulator-prod.zip"
RELEASE_OUT="$ROOT/../FlashMart-release-ios.zip"
rm -f "$OUT" "$RELEASE_OUT"
ditto -c -k --sequesterRsrc --keepParent "$APP" "$OUT"
cp -f "$OUT" "$RELEASE_OUT"
echo ""
echo "Simulator build: $APP"
echo "API: https://mart-api-95628498734.asia-south1.run.app"
echo "Zip: $OUT"
echo "Release zip: $RELEASE_OUT"
echo ""
echo "Install on booted simulator:"
echo "  xcrun simctl install booted \"$APP\""
echo "  xcrun simctl launch booted com.knsrmart.flashmart"
