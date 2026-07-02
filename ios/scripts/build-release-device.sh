#!/usr/bin/env bash
# Build FlashMart Release for physical iPhones (production Cloud Run API).
#
# Requires: Xcode, CocoaPods, Apple Developer account.
# Set IOS_DEVELOPMENT_TEAM to your 10-char Team ID (defaults to auto-detect from keychain).
#
# Distribution:
#   - development: install on devices registered in your Apple Developer account
#   - ad-hoc: set IOS_EXPORT_METHOD=ad-hoc and register client device UDIDs first
#
# Output: Mart/FlashMart-release-ios.zip (contains Flashmart.ipa)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! xcodebuild -version >/dev/null 2>&1; then
  echo "Install Xcode, then: sudo xcode-select -s /Applications/Xcode.app/Contents/Developer"
  exit 1
fi

TEAM="${IOS_DEVELOPMENT_TEAM:-}"
if [[ -z "$TEAM" ]]; then
  TEAM="$(security find-certificate -c 'Apple Development' -p 2>/dev/null \
    | openssl x509 -noout -subject 2>/dev/null \
    | sed -n 's/.*OU=\([^,]*\).*/\1/p' | head -1 || true)"
fi
if [[ -z "$TEAM" ]]; then
  echo "Set IOS_DEVELOPMENT_TEAM to your Apple Developer Team ID."
  exit 1
fi

EXPORT_METHOD="${IOS_EXPORT_METHOD:-development}"
echo "Team: $TEAM  Export: $EXPORT_METHOD  API: production (Release)"

xcodegen generate
pod install

EXPORT_PLIST="$(mktemp)"
trap 'rm -f "$EXPORT_PLIST"' EXIT
cat > "$EXPORT_PLIST" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>method</key>
  <string>${EXPORT_METHOD}</string>
  <key>teamID</key>
  <string>${TEAM}</string>
  <key>signingStyle</key>
  <string>automatic</string>
  <key>compileBitcode</key>
  <false/>
</dict>
</plist>
EOF

ARCHIVE="${TMPDIR:-/tmp}/Flashmart-Release.xcarchive"
IPA_DIR="${TMPDIR:-/tmp}/Flashmart-Release-ipa"
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

xcodebuild \
  -exportArchive \
  -archivePath "$ARCHIVE" \
  -exportPath "$IPA_DIR" \
  -exportOptionsPlist "$EXPORT_PLIST" \
  -allowProvisioningUpdates

IPA="$IPA_DIR/Flashmart.ipa"
OUT_ZIP="$ROOT/../FlashMart-release-ios.zip"
rm -f "$OUT_ZIP"
ditto -c -k --sequesterRsrc "$IPA" "$OUT_ZIP"

echo ""
echo "IPA: $IPA"
echo "Zip for sharing: $OUT_ZIP"
echo ""
if [[ "$EXPORT_METHOD" == "development" ]]; then
  echo "Install on registered test devices via Apple Configurator, Xcode Devices, or MDM."
  echo "Client iPhones must be added to your Apple Developer account (Devices) first."
elif [[ "$EXPORT_METHOD" == "ad-hoc" ]]; then
  echo "Ad Hoc IPA — install on devices listed in the provisioning profile."
fi
