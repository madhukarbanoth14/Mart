# Google Play Store — FlashMart submission guide

App ID: `com.knsrmart.flashmart`  
Production API: `https://mart-api-95628498734.asia-south1.run.app`

## 1. One-time: Play Console account

1. Pay **$25** at [play.google.com/console](https://play.google.com/console).
2. **Create app** → name **FlashMart** → default language **English**.
3. Complete **App access**, **Ads**, **Content rating**, **Target audience**, **News app**, **COVID-19**, **Data safety** (required before production).

## 2. One-time: Upload keystore (on your Mac)

```bash
cd Mart/android
chmod +x scripts/setup-play-upload-keystore.sh scripts/build-release-aab.sh
./scripts/setup-play-upload-keystore.sh
```

Back up `flashmart-upload.jks` and passwords in a password manager.

## 3. Build the release bundle (AAB)

```bash
cd Mart/android
./scripts/build-release-aab.sh
```

Output: `Mart/FlashMart-release.aab`

Play Store requires **AAB**, not APK.

## 4. Store listing (Play Console → Main store listing)

| Field | Suggestion |
|-------|------------|
| App name | FlashMart |
| Short description | B2B distribution for shopkeepers and dealers |
| Full description | Your product pitch (orders, inventory, onboarding, payments) |
| App icon | 512×512 PNG |
| Feature graphic | 1024×500 PNG |
| Phone screenshots | At least 2 (1080×1920 or similar) |

**Privacy policy URL** (required): host a page describing account data, documents, and payments.

## 5. Data safety form

Declare roughly:

- **Account info**: email, name, phone (collected, for app functionality)
- **Photos/files**: onboarding documents (collected)
- **Financial info**: payments via Razorpay (processed by third party)
- Data encrypted in transit (HTTPS)

## 6. Upload for your 2 testers (Internal testing)

1. Play Console → **Testing** → **Internal testing**.
2. **Create new release** → upload `FlashMart-release.aab`.
3. **Testers** → create email list → add both members’ Gmail addresses.
4. Share the **opt-in link** from Internal testing page.
5. Testers open link on Android → accept → install from Play Store.

No sideloading; updates are automatic when you upload new AABs.

## 7. Production release

After internal testing:

1. Fix any bugs.
2. Bump `versionCode` / `versionName` in `app/build.gradle.kts`.
3. Rebuild AAB → upload to **Production** (or Closed testing first).
4. Submit for review (typically 1–7 days).

## 8. Play App Signing

On first upload, Google asks to enroll in **Play App Signing**. Choose **Google manages app signing** (recommended). You keep the upload keystore; Google holds the app signing key.

## Checklist

- [ ] Play Console account ($25)
- [ ] Upload keystore created (`setup-play-upload-keystore.sh`)
- [ ] `local.properties`: demo flags **false**, production API URL set
- [ ] `FlashMart-release.aab` built
- [ ] Privacy policy URL live
- [ ] Store listing + screenshots
- [ ] Data safety + content rating complete
- [ ] Internal testing with 2 members
- [ ] Production submit

## Version bumps (each release)

Edit `Mart/android/app/build.gradle.kts`:

```kotlin
versionCode = 2        // must increase every upload
versionName = "1.0.1"
```

Then `./scripts/build-release-aab.sh` again.
