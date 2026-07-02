#!/usr/bin/env bash
# Create upload keystore + keystore.properties for Google Play (run once, keep backups).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

KEYSTORE="flashmart-upload.jks"
PROPS="keystore.properties"
ALIAS="flashmart"

if [[ -f "$KEYSTORE" ]]; then
  echo "Keystore already exists: $ROOT/$KEYSTORE"
  echo "Delete it first if you need a new one."
  exit 1
fi

echo "Create Play Store UPLOAD keystore (back up the .jks file and passwords safely)."
echo ""
read -r -s -p "Keystore password: " STORE_PASS
echo ""
read -r -s -p "Confirm keystore password: " STORE_PASS2
echo ""
if [[ "$STORE_PASS" != "$STORE_PASS2" ]]; then
  echo "Passwords do not match."
  exit 1
fi
read -r -s -p "Key password (Enter for same as keystore): " KEY_PASS
echo ""
KEY_PASS="${KEY_PASS:-$STORE_PASS}"

keytool -genkey -v \
  -keystore "$KEYSTORE" \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias "$ALIAS" \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=FlashMart, OU=Mobile, O=KNSR Mart, L=Hyderabad, ST=Telangana, C=IN"

cat > "$PROPS" <<EOF
storeFile=$KEYSTORE
storePassword=$STORE_PASS
keyAlias=$ALIAS
keyPassword=$KEY_PASS
EOF
chmod 600 "$PROPS" "$KEYSTORE"

echo ""
echo "Created:"
echo "  $ROOT/$KEYSTORE"
echo "  $ROOT/$PROPS"
echo ""
echo "IMPORTANT:"
echo "  - Never commit these files (listed in android/.gitignore)."
echo "  - Back up the .jks and passwords — you need them for every Play update."
echo "  - Google Play App Signing: enroll when you upload the first AAB."
echo ""
echo "Next:"
echo "  ./scripts/build-release-aab.sh"
