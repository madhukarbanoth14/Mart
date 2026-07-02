#!/usr/bin/env bash
# Build FlashMart Release for physical iPhones (production Cloud Run API).
#
# Requires: Xcode, CocoaPods, Apple Developer account signed into Xcode.
# Set IOS_DEVELOPMENT_TEAM to your 10-char Team ID (defaults to auto-detect from keychain).
#
# Distribution (IOS_EXPORT_METHOD):
#   - development  — install on registered test devices (default)
#   - ad-hoc       — install on devices with UDIDs in the profile
#   - app-store    — App Store / TestFlight upload (alias for app-store-connect)
#   - app-store-connect — same as app-store (preferred on Xcode 15+)
#
# App Store example:
#   IOS_EXPORT_METHOD=app-store ./scripts/build-release-device.sh
#
# Output:
#   - development/ad-hoc: Mart/FlashMart-release-ios.zip (contains Flashmart.ipa)
#   - app-store:          Mart/FlashMart-appstore.ipa and Mart/FlashMart-appstore.zip
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! xcodebuild -version >/dev/null 2>&1; then
  echo "Install Xcode, then: sudo xcode-select -s /Applications/Xcode.app/Contents/Developer"
  exit 1
fi

detect_team_id() {
  local cert
  for cert in 'Apple Distribution' 'Apple Development' 'iPhone Distribution' 'iPhone Developer'; do
    local team
    team="$(security find-certificate -c "$cert" -p 2>/dev/null \
      | openssl x509 -noout -subject 2>/dev/null \
      | sed -n 's/.*OU=\([^,]*\).*/\1/p' | head -1 || true)"
    if [[ -n "$team" ]]; then
      echo "$team"
      return 0
    fi
  done
  return 1
}

TEAM="${IOS_DEVELOPMENT_TEAM:-}"
if [[ -z "$TEAM" ]]; then
  TEAM="$(detect_team_id || true)"
fi
if [[ -z "$TEAM" ]]; then
  echo "Set IOS_DEVELOPMENT_TEAM to your Apple Developer Team ID."
  echo "Also sign in via Xcode → Settings → Accounts."
  exit 1
fi

RAW_EXPORT_METHOD="${IOS_EXPORT_METHOD:-development}"
case "$RAW_EXPORT_METHOD" in
  app-store|appstore|store)
    EXPORT_METHOD="app-store-connect"
  ;;
  *)
    EXPORT_METHOD="$RAW_EXPORT_METHOD"
  ;;
esac

echo "Team: $TEAM  Export: $EXPORT_METHOD  API: production (Release)"

xcodegen generate
pod install

write_export_plist() {
  local method="$1"
  local plist="$2"
  cat > "$plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>method</key>
  <string>${method}</string>
  <key>teamID</key>
  <string>${TEAM}</string>
  <key>signingStyle</key>
  <string>automatic</string>
  <key>compileBitcode</key>
  <false/>
  <key>uploadSymbols</key>
  <true/>
</dict>
</plist>
EOF
}

ARCHIVE="${TMPDIR:-/tmp}/Flashmart-Release.xcarchive"
IPA_DIR="${TMPDIR:-/tmp}/Flashmart-Release-ipa"
EXPORT_PLIST="$(mktemp)"
EXPORT_LOG="$(mktemp)"
trap 'rm -f "$EXPORT_PLIST" "$EXPORT_LOG"' EXIT
rm -rf "$ARCHIVE" "$IPA_DIR"

xcodebuild \
  -workspace Flashmart.xcworkspace \
  -scheme Flashmart \
  -configuration Release \
  -sdk iphoneos \
  -destination 'generic/platform=iOS' \
  -archivePath "$ARCHIVE" \
  DEVELOPMENT_TEAM="$TEAM" \
  CODE_SIGN_STYLE=Automatic \
  -allowProvisioningUpdates \
  archive

export_archive() {
  local method="$1"
  rm -rf "$IPA_DIR"
  write_export_plist "$method" "$EXPORT_PLIST"
  xcodebuild \
    -exportArchive \
    -archivePath "$ARCHIVE" \
    -exportPath "$IPA_DIR" \
    -exportOptionsPlist "$EXPORT_PLIST" \
    -allowProvisioningUpdates \
    >"$EXPORT_LOG" 2>&1
}

if ! export_archive "$EXPORT_METHOD"; then
  if [[ "$EXPORT_METHOD" == "app-store-connect" ]]; then
    echo "app-store-connect export failed; retrying with legacy app-store method..."
    if ! export_archive "app-store"; then
      cat "$EXPORT_LOG"
      exit 1
    fi
    EXPORT_METHOD="app-store"
  else
    cat "$EXPORT_LOG"
    exit 1
  fi
fi

IPA="$IPA_DIR/Flashmart.ipa"
if [[ ! -f "$IPA" ]]; then
  echo "Export succeeded but IPA not found at $IPA"
  ls -la "$IPA_DIR" || true
  exit 1
fi

if [[ "$EXPORT_METHOD" == "app-store-connect" || "$EXPORT_METHOD" == "app-store" ]]; then
  OUT_IPA="$ROOT/../FlashMart-appstore.ipa"
  OUT_ZIP="$ROOT/../FlashMart-appstore.zip"
else
  OUT_IPA="$ROOT/../FlashMart-release.ipa"
  OUT_ZIP="$ROOT/../FlashMart-release-ios.zip"
fi

cp -f "$IPA" "$OUT_IPA"
rm -f "$OUT_ZIP"
ditto -c -k --sequesterRsrc "$OUT_IPA" "$OUT_ZIP"

echo ""
echo "Archive: $ARCHIVE"
echo "IPA: $OUT_IPA"
echo "Zip: $OUT_ZIP"
echo ""

case "$EXPORT_METHOD" in
  development)
    echo "Install on registered test devices via Apple Configurator, Xcode Devices, or MDM."
    echo "Client iPhones must be added to your Apple Developer account (Devices) first."
    ;;
  ad-hoc)
    echo "Ad Hoc IPA — install on devices listed in the provisioning profile."
    ;;
  app-store-connect|app-store)
    echo "Upload to App Store Connect:"
    echo "  1. Open Transporter (Mac App Store) and drag $OUT_IPA"
    echo "  2. Or in Xcode: Window → Organizer → Archives → Distribute App"
    echo "  3. Or: xcrun altool --upload-app -f \"$OUT_IPA\" -t ios -u YOUR_APPLE_ID"
    ;;
esac
