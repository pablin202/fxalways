# iOS Host

The iOS app is a SwiftUI host for the shared Compose Multiplatform UI.

## Project Generation

The committed Xcode project is generated from `project.yml` with XcodeGen:

```bash
xcodegen generate
```

Open `FXAlways.xcodeproj` after generation.

The project links:

- `ComposeApp.framework` from `:composeApp`
- RevenueCat `PurchasesHybridCommon` `17.55.1` through SwiftPM
- RevenueCat iOS SDK `5.67.1`, resolved transitively by SwiftPM

`project.yml` builds the correct Compose framework automatically for the active Xcode destination:

- `iosArm64` for a physical iPhone
- `iosSimulatorArm64` for Apple Silicon simulators

It also copies Compose resources into the app bundle. Without this step iOS crashes on startup because fonts and generated Compose resources are missing.

## Local Build

Build the host app. Xcode will run the shared framework Gradle task through the `Build Compose framework` script phase.

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild \
  -project FXAlways.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 17' \
  build
```

For a physical iPhone:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild \
  -project FXAlways.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -destination 'id=00008150-00052DDA343A401C' \
  build
```

If iOS simulators are missing:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -downloadPlatform iOS
```

If Kotlin/Native cache errors appear, keep the script phase flag `-Pkotlin.native.cacheKind=none`.

## Smoke Test

Install and launch on a simulator:

```bash
DEVICE_ID="$(DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcrun simctl list devices available | rg 'iPhone 17 \\(' | head -1 | sed -E 's/.*\\(([A-F0-9-]{36})\\).*/\\1/')"
APP_PATH="$HOME/Library/Developer/Xcode/DerivedData/FXAlways-hgzorfcvkkusndfdkqhxzpqwzwzg/Build/Products/Debug-iphonesimulator/FX Always.app"

DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcrun simctl boot "$DEVICE_ID" || true
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcrun simctl install "$DEVICE_ID" "$APP_PATH"
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcrun simctl launch --console "$DEVICE_ID" com.fxalways.app.ios
```

Expected launch behavior:

- onboarding renders without crash
- after onboarding, Rates loads from `https://us-central1-moneytrackerpro-8ff64.cloudfunctions.net`
- `latestRates`, `historicalRates`, and `newsFeed` return `200 OK`
- foreground auto-refresh runs every 60 seconds

## Current Auth State

iOS currently creates a stable local anonymous id in `NSUserDefaults` so RevenueCat can configure without crashing.

Production still needs:

1. Add the iOS app to Firebase project `moneytrackerpro-8ff64`.
2. Add `GoogleService-Info.plist` to `iosApp/iosApp`.
3. Enable Sign in with Apple in Apple Developer and Firebase Auth.
4. Replace the iOS local guest implementation with Firebase Auth anonymous sign-in and Apple account linking.
5. Implement Firestore backup on iOS using the same `users/{uid}/backups/default` document contract as Android.

## RevenueCat

The iOS KMP config currently uses the RevenueCat Test Store key:

```text
test_aDOfCCMYLDGOStPsXdDkPJFanUC
```

Before TestFlight/App Store:

1. Create the iOS app/platform in RevenueCat.
2. Connect the App Store Connect subscription.
3. Attach the product to entitlement `pro`.
4. Add products to offering `default`.
5. Replace the Test Store key in `PlatformConfig.ios.kt` with the iOS public SDK key.
