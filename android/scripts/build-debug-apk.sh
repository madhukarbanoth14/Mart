#!/usr/bin/env bash
# Debug APK → local NestJS API (dummy OTP 123456 when NODE_ENV=development, no Twilio).
#
# Prerequisites:
#   1. Backend: cd Mart/backend && npm run start:dev  (PORT=3005 in .env)
#   2. android/local.properties → mart.api.base.url
#        Emulator:  http://10.0.2.2:3005
#        Phone:     http://YOUR_LAN_IP:3005  (same Wi‑Fi as laptop)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

API_URL="$(grep -E '^mart\.api\.base\.url=' local.properties 2>/dev/null | cut -d= -f2- || echo 'http://10.0.2.2:3005')"

chmod +x gradlew
./gradlew assembleDebug

APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
OUT="$ROOT/../FlashMart-debug-local.apk"
cp -f "$APK" "$OUT"

LAN_IP="$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null || true)"

echo ""
echo "Debug APK: $OUT"
echo "API URL (debug): $API_URL"
echo ""
echo "Start local API first:"
echo "  cd Mart/backend && npm run start:dev"
echo ""
echo "Dummy OTP: 123456 (dev API shows Dev OTP banner in registration)"
echo ""
if [[ "$API_URL" == *"10.0.2.2"* ]]; then
  echo "Emulator install:"
  echo "  adb install -r \"$OUT\""
  echo ""
  if [[ -n "$LAN_IP" ]]; then
    echo "Physical device? Set in android/local.properties:"
    echo "  mart.api.base.url=http://${LAN_IP}:3005"
    echo "  Then re-run this script."
  fi
else
  echo "Physical device install (USB debugging):"
  echo "  adb install -r \"$OUT\""
fi
