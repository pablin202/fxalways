# Android Release Check

Use this before uploading an Android build to Play Console.

## Required checks

```bash
./gradlew :composeApp:connectedDebugAndroidTest
./gradlew :composeApp:assembleRelease :composeApp:bundleRelease
```

Expected:

- `connectedDebugAndroidTest` passes.
- `assembleRelease` produces a release APK.
- `bundleRelease` produces a release AAB for Play Console.
- Release build has `BuildConfig.DEBUG=false`, so debug-only UI such as the local Pro/Free override is hidden.

## Signing

Release signing is configured from Gradle properties, `local.properties`, or environment variables:

```properties
ANDROID_KEYSTORE_PATH=/absolute/path/to/fxalways-release.jks
ANDROID_KEYSTORE_PASSWORD=...
ANDROID_KEY_ALIAS=fxalways
ANDROID_KEY_PASSWORD=...
```

Do not commit keystores or signing properties. The repo ignores `*.jks`, `*.keystore`, `release-key.properties`, and `keystore.properties`.

If these values are absent, Gradle can still compile an unsigned release artifact for verification. Play upload requires a signed AAB.

## Production config reminders

- `FX_BACKEND_URL` defaults to `https://us-central1-moneytrackerpro-8ff64.cloudfunctions.net`.
- `REVENUECAT_API_KEY` or `REVENUECAT_ANDROID_KEY` must be supplied for live purchases.
- `composeApp/google-services.json` currently targets Firebase project `moneytrackerpro-8ff64`.
