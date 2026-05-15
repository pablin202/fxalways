# FX Always Product Checklist

Use this as the release checklist. Mark items only after Android device testing and a KMP/iOS compile sanity check.

## Current Release Status

- [x] Firebase backend URL points to `moneytrackerpro-8ff64`.
- [x] Firebase Functions are deployed for latest rates, historical rates, supported currencies, news and crypto markets.
- [x] Crypto backend uses CoinPaprika, returns up to 200 assets and refreshes cache every 10 minutes.
- [x] App consumes HTTPS Functions instead of direct client Firestore reads.
- [x] Android debug build installs and opens on device.
- [x] iOS `iosArm64` compile passes.
- [x] UI supports English, Spanish, Portuguese and additional major languages.
- [x] Theme mode, app language and base currency persist.
- [x] Production builds hide the local dev premium override.
- [x] Legal links open `https://fxalways.com/legal?doc=privacy&lang={lang}` and `https://fxalways.com/legal?doc=terms&lang={lang}`.
- [x] README reflects the competitive feature set and recommended next product block.

## Release Blockers

- [ ] Finalize hosted legal document content for Privacy and Terms at `fxalways.com`.
- [x] Prepare internal testing checklist and store screenshot claim set.
- [ ] Produce final store listing text, screenshots and metadata for Android.
- [ ] Validate all critical release strings in the 13 supported languages.
- [ ] Run full Android instrumentation suite after the final commit.
- [x] Run Android release build check.
- [ ] Configure production Android keystore and verify signed Play upload AAB.
- [ ] Decide whether Android-only launch is acceptable before iOS production setup is complete.

## Android Launch Readiness

- [x] App icon is configured with padding.
- [x] Bottom navigation uses 5 primary destinations.
- [x] Android local notification channel is configured.
- [x] Android alert worker checks active alerts against backend rates.
- [x] Android widgets are implemented for rates and traveler.
- [x] Move notification permission request to a contextual moment.
- [ ] Confirm widgets refresh correctly after fresh install, app kill and backend cache refresh.
- [ ] Validate offline/slow-network first launch on a clean install.

## iOS Launch Readiness

- [x] Shared Compose code compiles for iOS arm64.
- [x] RevenueCat KMP gateway exists for iOS.
- [ ] Create production iOS Firebase app and add `GoogleService-Info.plist`.
- [ ] Enable Sign in with Apple in Apple Developer and Firebase Auth.
- [ ] Replace RevenueCat Test Store key with App Store public SDK key.
- [ ] Add iOS local notification implementation.
- [ ] Add iOS Widget Extension or explicitly defer iOS widgets.
- [ ] Install and smoke-test on connected iPhone before iOS release.

## Rates And Crypto

- [x] Loads latest fiat rates from Firebase backend.
- [x] Loads crypto market data from Firebase backend.
- [x] Free crypto starts with BTC, ETH, USDT and USDC.
- [x] Pro can search and add from expanded crypto catalog.
- [x] Base currency changes reload rates.
- [x] Refresh action is available.
- [x] Falls back gracefully when backend is unavailable.
- [x] Home crypto snapshot uses readable spacing/padding.
- [x] Home, Convert and Detail show rate source, freshness, cached/live status and indicative-rate disclaimer.
- [x] Show explicit loading skeletons for slow networks.
- [ ] Confirm stale/cached timestamp is accurate across fiat and crypto.
- [ ] Validate unavailable currency codes are hidden or explained.

## Convert

- [x] Uses live rates from selected base currency.
- [x] Fee comparison has Free/Pro gating.
- [x] Amount input is editable and preserves focus with large values.
- [x] Reverse pair action exists.
- [x] Free users can edit a limited converter currency list.
- [x] Pro users can use every supported converter currency.
- [x] Profile onboarding can preselect converter currencies and amount.
- [ ] Validate decimal separators for locales using comma.
- [ ] Confirm crypto precision does not round important digits in high-value conversions.
- [ ] Edge: selected target removed from backend falls back to a valid currency.

## Compare

- [x] Uses live rates from selected base currency.
- [x] Free users see limited comparison set.
- [x] Pro users can select from the expanded supported set.
- [x] Edit Comparison modal avoids nested scroll overlap.
- [x] Selected currencies apply clearly and update Home/Compare after Apply.
- [x] Empty compare data shows a useful state.
- [x] Profile onboarding can preselect compare currencies.
- [ ] Make overlay chart series reflect selected currencies.
- [ ] Visually verify Free limit and Pro unlimited compare list on Android and iOS.

## Alerts

- [x] Alerts persist locally on Android/iOS.
- [x] Free users are limited to 1 alert; Pro is unlimited.
- [x] Supports Above/Below target-rate alerts.
- [x] Supports Daily Move alert type.
- [x] Supports quick presets: -1%, -0.5%, +0.5%, +1%.
- [x] Debug test notification action exists per alert.
- [x] Alerts keep their original base pair when app base currency changes.
- [x] Duplicate identical alerts reactivate the existing alert.
- [ ] Add triggered-alert history.
- [ ] Add server-side alert evaluation for more reliable background delivery.
- [ ] Verify upgrade path when user hits the Free alert limit.
- [ ] Edge: offline restore failure copy is clear and does not change entitlement state.

## News

- [x] News feed comes from Firebase backend.
- [x] News is gated by Free/Pro story count.
- [x] Backend strategy supports language, region and currencies.
- [x] Region selector exists.
- [x] Currency filters exist.
- [x] Topic filters exist.
- [x] Article/source links open when a provider URL exists.
- [x] Empty states exist for no stories and filtered no-results.
- [x] Offline/no-connection messaging avoids showing the wrong news copy.
- [x] Empty "no news" box is full width.
- [x] Cache age and refresh feedback exist.

## Traveler

- [x] Shows local cheat sheet and etiquette card.
- [x] Free/Pro cheat sheet gating exists.
- [x] Manual destination selector exists.
- [x] Last travel destination persists offline.
- [x] Traveler widget exists on Android.
- [x] Profile onboarding can preselect traveler destination and amount.
- [ ] Add actual location/country detection with permission flow.
- [ ] Add country-specific ATM/card/tipping data source.
- [ ] Handle countries with multiple accepted currencies.

## Settings And Account

- [x] Theme mode persists.
- [x] Language selector exists.
- [x] Base currency persists and updates app rates.
- [x] Version name is visible.
- [x] Restore purchase calls RevenueCat restore.
- [x] Manage subscription deep links are present.
- [x] Notification permission/status row exists.
- [x] Legal/privacy rows open language-aware URLs.
- [x] Account backup/sync exists.
- [x] User profile can be changed after onboarding.

## Watchlist And Portfolio

- [x] Watchlist persists locally on Android/iOS.
- [x] Free users are limited to 4 tracked currencies; Pro is unlimited.
- [x] Users can add/remove currencies.
- [x] Tracked rows open detail screen.
- [x] Portfolio holdings persist by currency code.
- [x] Large holding inputs no longer jump/focus out.
- [x] Portfolio total recalculates when base currency changes.
- [x] Portfolio supports holding in the current base currency at rate 1.0.
- [x] Allocation percentages exist.
- [x] Daily portfolio move estimate exists.
- [x] Portfolio Pro supports average cost, cost basis, realized/unrealized P&L and transactions.
- [x] CSV import/export exists for portfolio holdings and transactions.
- [ ] Add multiple watchlists for Pro.
- [ ] Add manual rename for watchlist.
- [ ] Confirm holdings behavior when a currency is removed from watchlist.

## Monetization

- [x] Free/Pro policy is centralized.
- [x] Gating exists for alerts, watchlist, compare, news, traveler, fees, crypto and base currencies.
- [x] RevenueCat Android gateway is connected.
- [x] RevenueCat iOS gateway is connected.
- [x] Entitlement validation for `pro` exists.
- [x] Restore purchase behavior exists.
- [x] Purchase/restore in-progress state avoids duplicate taps.
- [x] Paywall has profile-aware copy and full-width primary CTA.
- [x] Paywall legal links are language-aware.
- [x] Pro offering is monthly/annual only; lifetime purchase is not exposed.
- [x] Paywall copy clearly sells monthly/annual recurring Pro value.
- [ ] Finalize production pricing and App Store/Play Store products.
- [ ] Test offline entitlement cache.
- [ ] Add paywall experiment hooks only if we actually plan to A/B test before launch.

## Personalization

- [x] Onboarding asks for Traveler, Crypto holder, Remittances, Freelancer or Savings.
- [x] Profile persists in settings and backup.
- [x] Home shows profile-aware Free/Pro focus, suggested pair and suggested alert.
- [x] Profile can preselect converter currencies, compare currencies, watchlist, traveler destination and suggested amount when defaults are still untouched.
- [x] Paywall adapts copy to selected profile.
- [x] App opens on Rates/Home instead of jumping to Watchlist after profile selection.
- [x] Suggested profile alert can be created/reactivated from Home in one tap.
- [ ] For remittances/freelancer, persist suggested provider and frequent amount as explicit user preferences.

## UI Test Coverage

- [x] Dashboard tests cover Free/Pro crypto and profile card behavior.
- [x] Onboarding tests cover profile selection.
- [x] Settings tests cover profile changes and language-aware legal URLs.
- [x] Paywall tests cover Free/Pro comparison, profile offer and language-aware legal URLs.
- [x] Converter tests cover large values and currency selection behavior.
- [x] Compare tests cover edit list and Free/Pro limits.
- [x] Traveler tests cover budget behavior.
- [x] Watchlist/portfolio tests cover holdings and import/export behavior.
- [x] Add a final "launch smoke" test class that walks Home -> Convert -> Compare -> Alerts -> More.

## Recommended Next Work

1. Close release blockers: legal content, store listing, release build/signing and full test run.
2. Add triggered-alert history.
3. Add provider-history insight to the fee comparator.
4. Persist suggested provider/frequent amount for remittance and freelancer profiles.
5. Decide iOS release scope: compile-only, TestFlight, or full App Store setup.
