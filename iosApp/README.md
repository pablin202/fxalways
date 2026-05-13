# iOS host

Create an Xcode iOS App target named `iosApp`, then add these Swift files and link the generated `ComposeApp` framework from `:composeApp`.

For local development:

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

The shared entry point is `MainViewController()` in `composeApp/src/iosMain/kotlin/com/fxalways/app/MainViewController.kt`.

Before App Store/TestFlight:

1. Confirm `moneytrackerpro-8ff64` in `PlatformConfig.ios.kt`.
2. Replace `appl_YOUR_REVENUECAT_PUBLIC_IOS_KEY`.
3. Configure the monthly auto-renewable subscription in App Store Connect.
4. Configure the matching entitlement and offering in RevenueCat.
