# RevenueCat KMP Setup

The app uses the official RevenueCat Kotlin Multiplatform SDK from `commonMain`.
RevenueCat receives a stable app user id as `appUserID`. Android uses the Firebase Auth UID. iOS currently uses a stable
local anonymous id until Firebase Auth + Sign in with Apple are connected.

## 1. Project

1. Create or open the RevenueCat project for FX Always.
2. Create an entitlement:
   - Identifier: `pro`
   - Description: FX Always Pro access.
3. During development, a RevenueCat Test Store key can be used:

```properties
REVENUECAT_API_KEY=test_your_public_sdk_key
```

For production, use the public SDK key for the active store/platform configured in RevenueCat. The key is public, but keep it
in `local.properties` or CI secrets so builds can swap environments cleanly.

## 2. Local Config

Add this to `local.properties` in the project root:

```properties
REVENUECAT_API_KEY=test_aDOfCCMYLDGOStPsXdDkPJFanUC
```

Gradle exposes it as `BuildConfig.REVENUECAT_API_KEY`, then `PlatformConfig.revenueCatApiKey` passes it to the shared KMP subscription gateway.

If the key is empty, the app disables live purchase actions and shows a clear paywall status message.

## 3. Products

For Test Store or store-backed setup, keep these identifiers consistent:

```text
Entitlement: pro
Offering: default
Package: monthly
Package: annual
```

Suggested production subscription product ids:

```text
fxalways_pro_monthly
fxalways_pro_annual
```

The app reads `offerings.current.monthly` and `offerings.current.annual`. Production should not configure a lifetime
package for this offering; FX Always sells Pro as monthly or annual recurring subscriptions only.

## 4. Android Production

When moving from Test Store to Google Play:

1. Create the app in RevenueCat with package `com.fxalways.app`.
2. Connect Google Play in RevenueCat.
3. Create monthly and annual Google Play subscriptions or base plans.
4. Attach the Play product to entitlement `pro`.
5. Add them to the current offering as the monthly and annual packages.
6. Install from an internal/test Play track for real billing tests.

Raw `adb install` builds are fine for UI and Test Store checks, but Google Play billing usually needs a Play testing track,
tester account and active product setup.

## 5. iOS Production

When preparing iOS:

1. Create monthly and annual App Store Connect subscriptions.
2. Add the iOS app/platform in RevenueCat.
3. Attach the iOS products to entitlement `pro`.
4. Keep offering `default` and monthly/annual packages aligned with Android.
5. Provide the iOS public SDK key through `PlatformConfig.ios.kt` or an iOS build configuration.

There is no separate iOS subscription gateway in this app; purchase logic stays in `commonMain`.
The project is currently pinned to RevenueCat KMP `2.10.2+17.55.1` because the app uses Kotlin `2.1.21`. RevenueCat KMP
`3.0.0` requires newer Kotlin metadata, so move to `3.0.0` when the KMP/Compose toolchain is upgraded.

The iOS host links RevenueCat through SwiftPM in `project.yml`:

```text
purchases-hybrid-common 17.55.1
purchases-ios-spm 5.67.1
```

## 6. App Behavior

Expected behavior after setup:

1. App starts with a stable app user id.
2. Shared KMP code configures RevenueCat with that id.
3. Paywall fetches the current offering and displays the RevenueCat price.
4. Purchase calls RevenueCat from common code.
5. Active entitlement `pro` unlocks:
   - unlimited alerts
   - unlimited watchlist currencies
   - all base currencies
   - full news
   - extended history label
   - full fee comparison
6. Restore calls RevenueCat restore and refreshes the same entitlement.

## 7. Verification

Run:

```bash
./gradlew :composeApp:assembleDebug
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew --no-daemon \
  :composeApp:compileKotlinIosSimulatorArm64 \
  :composeApp:linkDebugFrameworkIosSimulatorArm64 \
  -Pkotlin.native.cacheKind=none
```

Install on the S20+:

```bash
adb -s R58N60PP4BM install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
adb -s R58N60PP4BM shell am force-stop com.fxalways.app
adb -s R58N60PP4BM shell monkey -p com.fxalways.app 1
```

Manual QA:

1. Confirm `Settings > Backup` shows the Firebase account.
2. Open paywall.
3. Confirm price comes from RevenueCat.
4. Trigger purchase with Test Store or store tester.
5. Confirm Pro gates unlock immediately.
6. Force stop and reopen app.
7. Confirm Pro remains active.
8. Tap restore and confirm state remains Pro.

iOS smoke test:

```bash
./scripts/ios-simulator-run.sh
./scripts/ios-device-run.sh
```
