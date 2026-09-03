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

## Versioning

Release builds use `ANDROID_VERSION_CODE` when provided through Gradle properties, `local.properties`, or environment variables:

```bash
./gradlew :composeApp:bundleRelease -PANDROID_VERSION_CODE=5 -PANDROID_VERSION_NAME=1.0.2
```

GitHub Actions passes `github.run_number` as `ANDROID_VERSION_CODE`, so CI release artifacts get an increasing Play-compatible code automatically. Local builds default to the base values in `composeApp/build.gradle.kts`; increase `ANDROID_VERSION_CODE` before uploading manually to Play Console.

## Production config reminders

- `FX_BACKEND_URL` defaults to `https://us-central1-fx-always.cloudfunctions.net`.
- `REVENUECAT_API_KEY` or `REVENUECAT_ANDROID_KEY` must be supplied for live purchases.
- `composeApp/google-services.json` currently targets Firebase project `fx-always`.
