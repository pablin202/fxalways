# FX Always

A Kotlin Multiplatform app built with Compose for currency exchange, with current and historical rates, a global watchlist, crypto, a portfolio, alerts, traveler mode, and Pro subscriptions through RevenueCat.

## Architecture

- `composeApp`: Shared Compose UI for Android and iOS.
- `functions`: Firebase Cloud Functions written in TypeScript.
- Firestore: Backend cache for `latest` and `history` to avoid thousands of calls to the FX provider.
- Initial FX provider: Frankfurter API, based on European Central Bank data.
- Premium: `SubscriptionGateway` integrates with RevenueCat KMP for Android and iOS.
- Android release: Signed AAB for Google Play Internal Testing, with a `versionCode` that CI can generate automatically.

## Features

- Multi-currency converter.
- Watchlist with major currency pairs.
- 12 months of rate history for Free and 5 years for Pro.
- Sparkline for each pair.
- Basic portfolio based on the watchlist, with amounts by currency, total value in the base currency, estimated daily change, and percentage weights.
- Pro portfolio with average cost, cost basis, realized and unrealized P&L, fiat and crypto allocation, largest position, an estimated portfolio value chart, and buy/sell transaction history.
- Pro portfolio CSV import and export for holdings, average cost, and transactions.
- Live crypto catalog with core assets for Free and an expanded catalog for Pro.
- Estimated cost comparison by provider, amount, and currency pair, showing the best provider, amount received, loss relative to the mid-market rate, and potential savings.
- Conversion decision support: Home recommends the next action, Convert summarizes timing, the best route, and an alert, and Provider Matrix distinguishes live quotes, comparisons, estimates, and setup.
- Target rate and daily move alerts.
- Server-side alerts via FCM for Android, with a local worker as a fallback.
- OCR price scanner for Pro on Android, integrated into the local-versus-live price check.
- Traveler mode with a local budget, cash buffer, and spending cheat sheet.
- User backup and sync on Android with Firebase.
- Personalized onboarding with Traveler, Crypto holder, Remittances, Freelancer, and Savings profiles. The selected profile sets the initial pairs, watchlist, travel destination, starting amount, Home screen, and paywall.
- Pro paywall with RevenueCat KMP, purchase restoration, Terms and Privacy links, and separate Android and iOS configuration.
- Recurring monthly and annual Pro subscriptions. There is no lifetime purchase option.
- Rate trust indicators on Home, Convert, and Detail show the source, Live/Cache/Preview status, and last update time.
- Loading skeletons when rates, the converter, or news are slow to load.
- Basic analytics for plan selection, successful purchases, currencies added, and widget usage.
- In-app language selector with four options: English, Español, Português, and हिन्दी. Dictionaries for nine additional languages are gated behind `EXTENDED_LANGUAGES = false`. Run `python3 scripts/i18n-audit.py` to list untranslated keys.
- Server-side caching by base currency and historical caching by pair.
- Scheduled refresh every 60 minutes.
- Firestore is closed to direct client access. The app uses HTTPS Functions exclusively.
- Separate Android and iOS configuration.

## Free vs Pro

`composeApp/src/commonMain/kotlin/com/fxalways/app/subscription/FeatureAccessPolicy.kt` is the source of truth. The landing page, Play Store listing, and paywall mirror this table and must be updated together.

| Capability | Free | Pro (US$2.99/month · US$19.99/year) |
|---|---|---|
| Converter, favorites, comparison | Unlimited currencies | Same |
| Real cost by provider | Your broker, 3 providers | All providers plus a history of which provider was cheapest |
| Rate alerts | 2 active | Unlimited plus “best time” and “cheapest provider today” |
| History | 1 year | 5 years (all-time) where available |
| Crypto | BTC, ETH, USDT, USDC | Expanded catalog (up to 200) |
| Portfolio | Basic holdings | Average cost, realized and unrealized P&L, transactions, CSV |
| Traveler | 1 destination, basic local rules | All destinations, offline packs, OCR price scanning |
| News | Top 3 | Full feed by region |

Free provides broad access so the app is useful before users upgrade. Pro adds deeper analysis and automation.

## Unit tests (commonTest)

`./gradlew :composeApp:testDebugUnitTest` runs the business logic tests without a device: `SmartTimingTest`,
`FeeQuotesTest`, `PortfolioCsvTest`, `BackupMergeTest`, `MoneyFormattingTest`, `FeatureAccessPolicyTest`,
`ReviewPromptPolicyTest`, `LocalizationTest`, and `AmountBucketTest`. CI runs them before building the
Test Lab artifacts. Instrumented tests (`connectedDebugAndroidTest`) cover the UI.

## Release build (R8)

`release` builds run R8 with `isMinifyEnabled` + `isShrinkResources` and the rules in
`composeApp/proguard-rules.pro` (kotlinx.serialization models, manifest components, enums persisted by
name, Firestore reflection, ML Kit / CameraX, RevenueCat, Ktor). The Crashlytics Gradle plugin uploads
`mapping.txt` on every release build and CI also attaches it as the `android-release-mapping-<versionCode>`
artifact; the AAB carries the deobfuscation file for Play automatically.

Without a release keystore the release build type is signed with the debug key so a minified APK can be
installed locally: `./gradlew :composeApp:assembleRelease` then `adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk`.
When adding a new `@Serializable` model or a class reached via reflection, smoke-test the release build on a device.

## Account deletion

Go to Settings → Backup → **Delete account**. Guest users can also delete their accounts. After confirmation, the app deletes `users/{uid}` and its subcollections (`backups`, `push_tokens`, `server_alert_events`) from Firestore, deletes the Firebase Auth user (reauthenticating with Google if needed), signs out of RevenueCat, clears all local preferences, and returns to onboarding. Purchases remain associated with the Google account. The account deletion URL declared in Play Console must point to a page on fxalways.com that describes these steps.

## Competitive roadmap

For the detailed benchmark and market-specific roadmap, see [PRODUCT_BENCHMARK_ROADMAP.md](PRODUCT_BENCHMARK_ROADMAP.md).

To compete with modern FX converters, money transfer apps, and crypto trackers, FX Always should prioritize features that improve daily retention and increase the value of Pro.

Current positioning: FX Always helps users make FX decisions and does not transmit money. The app shows mid-market rates, estimated fees and spreads, provider status, alerts, and context to help users decide before converting, paying, or sending money. Recommended Play Store screenshot copy: “Know when to convert”, “Compare real provider cost”, “Scan prices abroad”, “Get push alerts”, “Track your multi-currency money”.

1. Full Pro portfolio with P&L and allocation. Implemented in the initial version.
   - Cash by currency, crypto holdings, and stablecoins.
   - Average purchase price and cost basis.
   - Daily, total, realized, and unrealized P&L.
   - Total value in the base currency.
   - Allocation by asset, asset type, and currency.
   - Chart of estimated portfolio value over time.
   - Buy/sell transaction history with realized P&L.
2. Smart conversion timing. Implemented in the initial version.
   - Rating by pair: Good time, Wait, Strong rate.
   - Comparison against 7-day, 30-day, and 90-day ranges, trends, and volatility.
   - Specific recommendations for travel, savings, and remittances.
3. Android and iOS widgets. Android widgets were implemented in the initial version.
   - Favorite rate from the live cache.
   - BTC and ETH when available in the cache.
   - Live/Cache status and a deep link to the app.
   - The iOS Widget Extension and total portfolio value are pending.
4. Smart alerts.
   - Portfolio movement by percentage or amount.
   - Excessive concentration in one asset.
   - Exchange rate near its 30-day high or low.
   - Best estimated provider for a conversion.
5. Import, export, and robust backup. Portfolio CSV support was implemented in the initial version.
   - CSV import and export of holdings and transactions.
   - Cross-platform cloud backup.
   - Editable transaction history.
6. A more actionable cost comparison. The main summary is implemented.
   - Approximate amount you will receive after fees and spreads.
   - Difference from the mid-market rate.
   - Best estimated provider by amount and currency pair.
   - Potential savings compared with the worst provider shown.
   - History of the cheapest provider is pending.
7. Personalized onboarding. Implemented in the initial version.
   - Profiles for travelers, crypto holders, remittances, freelancers, and savings.
   - Home, initial pairs, watchlist, travel destination, suggested amount, paywall, and suggested alerts based on the user's goal.

The P0 items were implemented and tested: fee and spread reality check, smart alerts with history, offline travel pack, watchlist groups, and expanded Android widgets with cache status.

## Run on Android

The deployed backend is configured by default.

```bash
./gradlew :composeApp:assembleDebug
```

To use the local Firebase emulator:

```bash
./gradlew :composeApp:assembleDebug -PFX_BACKEND_URL=http://10.0.2.2:5001/demo-fx-always/us-central1
```

To use the deployed Firebase backend:

```bash
./gradlew :composeApp:assembleDebug -PFX_BACKEND_URL=https://us-central1-fx-always.cloudfunctions.net
```

## Android release for Google Play

Build the Play Store AAB with:

```bash
./gradlew :composeApp:bundleRelease -PANDROID_VERSION_CODE=5 -PANDROID_VERSION_NAME=1.0.2
```

Output:

```text
composeApp/build/outputs/bundle/release/composeApp-release.aab
```

Configure release signing through `local.properties`, Gradle properties, or environment variables:

```properties
ANDROID_KEYSTORE_PATH=/absolute/path/to/fxalways-upload.jks
ANDROID_KEYSTORE_PASSWORD=...
ANDROID_KEY_ALIAS=fxalways
ANDROID_KEY_PASSWORD=...
REVENUECAT_API_KEY=goog_...
```

Do not commit keystores or passwords. The repository ignores `*.jks`, `*.keystore`, `release-key.properties`, `keystore.properties`, and `local.properties`.

### Automated GitHub release

Every push to `main` runs the following steps:

1. Build the Android debug APK and instrumented test APK.
2. Run Firebase Test Lab on SmallPhone, MediumPhone, and MediumTablet.
3. If Test Lab passes, build the signed release AAB for Google Play.
4. Publish the AAB and its `.sha256` checksum to GitHub Releases.

Automated versioning uses:


- `versionCode = github.run_number`
- `versionName = 1.0.<github.run_number>`
- Tag `android-v1.0.<github.run_number>`

Required GitHub secrets:


- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `REVENUECAT_API_KEY`

### Automatic version code

`composeApp/build.gradle.kts` reads:

- `ANDROID_VERSION_CODE`
- `ANDROID_VERSION_NAME`

Precedence: Gradle property, `local.properties`, environment variable, then the repository default.

Manual example:

```bash
./gradlew :composeApp:bundleRelease -PANDROID_VERSION_CODE=5 -PANDROID_VERSION_NAME=1.0.2
```

GitHub Actions uses `github.run_number` as `ANDROID_VERSION_CODE`, so CI artifacts automatically get increasing version codes.

### Firebase Test Lab in CI

Every push to `main` runs all Android UI tests in Firebase Test Lab. The workflow:

- Builds the debug APK and the `androidTest` APK.
- Queries the current Test Lab catalog with `gcloud firebase test android models list`.
- Selects three available devices from different device families and screen resolutions when the catalog offers them.
- Runs `gcloud firebase test android run` as an instrumentation test without `test-targets` to execute the full suite.

Required GitHub configuration:

- Repository variable `FIREBASE_TEST_LAB_PROJECT`: Firebase/GCP project ID. Current default: `fx-always`.
- Repository variable `FIREBASE_TEST_LAB_RESULTS_BUCKET`: Cloud Storage bucket for results. Current default: `fx-always-test-lab-results`.
- Repository secret `GCP_WORKLOAD_IDENTITY_PROVIDER`: Workload Identity Federation provider for GitHub Actions.
- Repository secret `GCP_SERVICE_ACCOUNT`: Service account used by the workflow.

The service account needs permission to run Firebase Test Lab and write results to Cloud Storage. In GCP, grant permissions equivalent to Firebase Test Lab Admin on the project and Storage Object Admin on the results bucket used by Test Lab. Required APIs: `testing.googleapis.com` and `toolresults.googleapis.com`.

### Play Console warnings

- `No deobfuscation file`: Expected when `isMinifyEnabled=false`. There is no `mapping.txt` because that release does not use R8/ProGuard.
- `Native code without debug symbols`: The app includes `.so` files from dependencies. Release builds set `ndk.debugSymbolLevel = "SYMBOL_TABLE"`, but some dependencies may already have their symbols stripped. This warning does not block Internal Testing.

## RevenueCat and Android purchases

Expected RevenueCat configuration:

- Entitlement: `pro`.
- Offering: `pro` is recommended. The app also accepts `default` or the current offering as a fallback.
- Packages:
  - `$rc_monthly` -> `fxalways_pro_monthly`
  - `$rc_annual` -> `fxalways_pro_annual`
- Android SDK key: `goog_...`.
- Do not use a lifetime package.

Expected Google Play configuration:

- App package: `com.fxalways.app`.
- Exact subscription product IDs:
  - `fxalways_pro_monthly`
  - `fxalways_pro_annual`
- Internal testing track with a signed AAB.
- Add testers to both the internal track and license testing for sandbox purchases.

Installing with `adb` or `bundletool` does not validate real or sandbox purchases. If the device reports `installerPackageName=null`, Google Billing may be unavailable and the paywall may show `Purchases unavailable`. To test purchases, install from the Google Play Internal Testing opt-in link. The installer must be `com.android.vending`.

## Internal Testing checklist

1. Build a signed AAB with a new version code.
2. Upload it to Google Play Console → Testing → Internal testing.
3. Complete the required forms: store listing, data safety, app content, privacy policy, category, and contact details.
4. Confirm that the subscription products are active in Play and linked to RevenueCat.
5. Add testers to the internal track and to license testing.
6. Install from the Play internal testing link.
7. Test:
   - Full onboarding.
   - Home exchange rates and core crypto assets.
   - Monthly and annual options on the paywall.
   - Monthly sandbox purchase.
   - Purchase restoration.
   - Free and Pro feature access.
   - Terms and Privacy links.
   - Offline and cached states.
   - Widgets.

## Quick troubleshooting

- `All uploaded bundles must be signed`: One or more signing settings are missing: `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, or `ANDROID_KEY_PASSWORD`.
- `Version code already used`: Increase `ANDROID_VERSION_CODE` and rebuild the AAB.
- `Purchases unavailable` on a local install: Expected when installing with `adb`. Install from Play Internal Testing.
- Purchase-related crash after onboarding: RevenueCat supports anonymous initialization if the Firebase UID is not yet available.
- Crash when opening Terms or Privacy: Android now catches `ActivityNotFoundException` and shows `No browser available` if no browser is installed.

## Run the backend

```bash
cd functions
npm install
npm run build
cd ..
firebase emulators:start --only functions,firestore
```

Endpoints:

- `GET /latestRates?base=USD`
- `GET /historicalRates?base=USD&quote=EUR&days=365`
- `GET /providerCatalog?base=AUD`
- `GET /providerQuotes?base=AUD&target=ARS&amount=500&providers=wise,revolut,moneygram&plan=pro`

Provider quotes run through Firebase Functions so provider credentials stay off-device. Wise uses the official quote endpoint when reachable and Wise Comparison API for market comparison data across supported providers. Revolut and MoneyGram switch to live mode when `REVOLUT_API_TOKEN`, `MONEYGRAM_ACCESS_TOKEN` and `MONEYGRAM_PARTNER_ID` are configured in the backend environment; otherwise the endpoint returns explicit `comparison`, `partner_setup` or `estimated` statuses with source labels. Provider Matrix copy treats those states separately so production UI never implies an estimate or comparison is a locked provider quote.

Server-side alert evaluation runs as the scheduled `evaluateServerAlerts` Firebase Function every 15 minutes. It reads backed-up alert snapshots, evaluates target-rate and daily-change alerts against backend rates, writes `lastTriggeredAtMillis` back to the backup payload, records server alert events under each user and sends FCM push notifications to registered Android devices. Android also keeps the local alert worker as a fallback path.

## Deploy Firebase

Production project: `fx-always` (project number `75079929673`, Firestore in `nam5`, Functions in `us-central1`). Migrated from the shared `moneytrackerpro-8ff64` project on 2026-09-03 (issue #23).

```bash
firebase use fx-always
cd functions
npm install
npm run deploy
```

Secrets: `MARKETAUX_API_KEY` must exist in Secret Manager for the deploy to succeed. The literal value `unset` is a documented placeholder: `newsFeed` then falls back to GDELT. Set the real key with:

```bash
printf '%s' "$MARKETAUX_KEY" | firebase functions:secrets:set MARKETAUX_API_KEY --project fx-always --data-file=-
```

CI (Firebase Test Lab) authenticates through Workload Identity Federation: pool `github-pool`, provider `github-provider`, service account `github-ci@fx-always.iam.gserviceaccount.com`, results bucket `gs://fx-always-test-lab-results`. The GitHub secrets `GCP_WORKLOAD_IDENTITY_PROVIDER` / `GCP_SERVICE_ACCOUNT` and variables `FIREBASE_TEST_LAB_PROJECT` / `FIREBASE_TEST_LAB_RESULTS_BUCKET` point there.


## iOS

The iOS project is generated with XcodeGen. Xcode builds the appropriate Compose framework for a device or simulator.

```bash
./scripts/ios-simulator-run.sh
./scripts/ios-device-run.sh
```

Remaining iOS production setup:

1. Create the iOS app in Firebase and add `GoogleService-Info.plist`.
2. Enable Sign in with Apple in Apple Developer and Firebase Auth.
3. Replace the RevenueCat Test Store key with the public App Store SDK key.

## References

- Frankfurter API: https://frankfurter.dev/
- Firebase scheduled functions: https://firebase.google.com/docs/functions/schedule-functions
- RevenueCat KMP subscriptions: https://www.revenuecat.com/blog/engineering/cmp-subscriptions/
