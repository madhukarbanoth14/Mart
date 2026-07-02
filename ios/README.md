# Flashmart iOS

SwiftUI distribution app matching the Flashmart-2 design system and Android Mart demo.

## Requirements

- macOS with Xcode 15+
- [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`)
- [CocoaPods](https://cocoapods.org) (`brew install cocoapods`) — for Razorpay SDK

## Open & build

```bash
cd Mart/ios
xcodegen generate
pod install
open Flashmart.xcworkspace   # not .xcodeproj — use workspace after pod install
```

In Xcode, select the **Flashmart** scheme and an **iPhone 15** (or newer) simulator, then **⌘R**.

Without CocoaPods, the app still builds from `Flashmart.xcodeproj`, but Razorpay live checkout shows a fallback message. Demo/mock payments work without the SDK.

If `pod install` warns about base configuration, ensure `Config.xcconfig` and `Config.Release.xcconfig` include the Pods xcconfig files (already set up in this repo).

## First launch

New users see a **3-screen onboarding carousel** (Order → Deliver → Bill) from the Flashmart-2 design reference, then the login screen. Onboarding is shown once; reset with:

```bash
# Simulator: Device → Erase All Content and Settings
# Or delete the app and reinstall
```

## Configuration

| Setting | Debug (Xcode ⌘R) | Release / simulator zip |
|---------|------------------|-------------------------|
| `MART_API_BASE_URL` | `http://127.0.0.1:3005` | `https://mart-api-95628498734.asia-south1.run.app` |
| `MART_USE_LOCAL_DEMO_AUTH` | `YES` | `NO` |
| `MART_DEMO_MODE` | `YES` | `NO` |
| `MART_RAZORPAY_KEY_ID` | `rzp_test_YOUR_KEY_HERE` | your Razorpay test/live key |

Edit `Config.xcconfig` / `Config.Release.xcconfig`, then regenerate the project.

### Simulator zip (production API)

```bash
cd Mart/ios
./scripts/build-simulator-zip.sh
```

Produces `Mart/FlashMart-simulator-prod.zip` and `Mart/FlashMart-release-ios.zip` (Release config → Cloud Run). Install on a booted simulator:

```bash
xcrun simctl install booted /path/to/Flashmart.app
xcrun simctl launch booted com.knsrmart.flashmart
```

## Payments

| Mode | Behaviour |
|------|-----------|
| **Local demo** (`MART_USE_LOCAL_DEMO_AUTH=YES`) | Card / UPI / pay-later use mock payment — no Razorpay SDK needed |
| **Live API** | Checkout offers **Razorpay** (native SDK via CocoaPods) plus mock pay fallback |

**Razorpay test card:** `4111 1111 1111 1111` — any future expiry, any CVV.

Flow matches Android:
1. Cart → **Proceed to payment** → choose Card / UPI / Pay on delivery / Razorpay
2. Razorpay: `POST /orders/create` → `POST /payments/razorpay/order` → native checkout → `POST /payments/razorpay/verify`

## Demo accounts

Password for all seed accounts: **Password@123**

| Role | Email |
|------|-------|
| Admin | admin@martdemo.com |
| Employee | employee@martdemo.com |
| Dealer | dealer@martdemo.com |
| Shopkeeper | shop1@martdemo.com |
| Shopkeeper | shop2@martdemo.com |

With `MART_USE_LOCAL_DEMO_AUTH = YES` (Debug only), login works offline (in-memory demo store). Release builds and the simulator zip script use the production API — no local backend required.

## Backend (optional, Debug builds only)

```bash
cd Mart/backend
npm install && npm run start:dev
```

Simulator uses `127.0.0.1:3005` (not `localhost` — ATS allows loopback).
