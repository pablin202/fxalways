# FX Always Product Checklist

Use this as the release checklist. Mark items only after Android device testing and a KMP/iOS compile sanity check.

## Current Release Status

- [x] Firebase backend URL points to `fx-always`.
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
- [x] Product benchmark and cross-market roadmap are documented in `PRODUCT_BENCHMARK_ROADMAP.md`.
- [x] P7 commercial productization pass is implemented in Home, Convert, Alerts and documentation.
- [x] P8 retention loop pass is implemented in Home and Watchlist.
- [x] 14-day closed testing feedback is reviewed and documented.
- [x] P9 tester-feedback product reset is completed before Android production rollout.

## Release Blockers

- [ ] Finalize hosted legal document content for Privacy and Terms at `fxalways.com`.
- [x] Prepare internal testing checklist and store screenshot claim set.
- [ ] Produce final store listing text, screenshots and metadata for Android.
- [x] Complete P9 Android pre-production fixes from closed testing.
- [x] Validate all critical release strings in the 13 supported languages.
- [x] Run full Android instrumentation suite on S25 after the final UI polish pass.
- [x] Run Android release build check.
- [x] Configure production Android keystore and generate signed Play upload AAB locally.
- [ ] Upload signed AAB to Play internal testing and verify Play accepts it.
- [x] Android-only internal testing is acceptable while iOS production setup remains deferred.

## P9 Tester Feedback Product Reset

Use this checklist for the next product iteration. The goal is to make the app match the traveler promise from the store/onboarding while keeping advanced FX tools available behind explicit user intent.

Closed testing evidence:

- [x] Tester report rating reviewed: 4.6/5.
- [x] Stability reviewed: no crashes, freezes or major performance issues reported.
- [x] Strong positives captured: converter, Traveler, Compare, Watchlists, Alerts, News, Crypto and offline cached rates.
- [x] Improvement themes captured: first-time guidance, information density, Pro banner frequency, historical charts, favorite management, news personalization, travel planning, small text and empty states.
- [x] Production-access questionnaire answer 8 is ready to update with concrete completed changes after P9 implementation.

### P9.0 Traveler Home Simple

- [x] Add a Traveler-specific Home layout branch.
- [x] Show primary action tiles before dense market data:
  - [x] Check a price.
  - [x] Convert money.
  - [x] Tipping and local rules.
  - [x] Trip budget.
- [x] Keep only one main rate card before the action tiles or immediately after them.
- [x] Move detailed compare/market movement below the first Traveler task area.
- [x] Rewrite Traveler Home copy around tasks, not market jargon.
- [x] Ensure Free users see at most one upgrade surface in the first viewport.
- [x] UI test: Traveler action tile order is stable on phone.
- [x] UI test: every action tile routes to the intended workflow.
- [ ] UI test: small-phone layout does not clip action copy.

### P9.1 Tipping And Local Rules

- [x] Make Tipping/local rules reachable from Traveler Home in one tap.
- [x] Add/verify content blocks for:
  - [x] Tipping.
  - [x] Card acceptance.
  - [x] Cash needed.
  - [x] ATM warning.
  - [x] Dynamic currency conversion warning.
  - [x] Local price norms.
- [x] Add source/freshness labels for guide data.
- [x] Free: show country-level basics.
- [ ] Pro: show richer city/offline/cost-template detail.
- [x] Empty state: missing guide data stays full-width and useful.
- [x] UI test: local rules tile opens expected content.
- [x] UI test: Free and Pro local rules content stays visible.
- [x] i18n: all new guide copy is translated through the app translation layer.

### P9.2 Clickability Contract

- [x] Audit first two viewports of Home for clickable-looking static cards.
- [x] Audit Traveler cards and rows.
- [x] Audit Convert cards, provider rows and Pro locks.
- [x] Audit Compare metrics, tiles, board and overlay.
- [x] Audit More and Settings rows.
- [x] Add arrows only to surfaces that navigate.
- [x] Remove action styling from static informational cards.
- [x] Ensure rows that look actionable either call a real callback or request permission.
- [x] UI test: action cards click once and route/mutate correctly.
- [x] UI test: static cards do not fire navigation callbacks.
- [x] UI test: locked Free actions open paywall once.

### P9.3 Free Country/Destination Choice

- [x] Define final Free destination slot count: 1.
- [x] Replace fixed Free destination assumptions with user-selected destination slots.
- [x] Persist selected Free destinations in backup settings.
- [x] Migration: keep current traveler destination as the first Free slot.
- [x] Destination picker copy says Free includes selected destination(s), not a fixed country list.
- [x] Pro unlocks all supported destinations.
- [x] Confirm widgets and offline snapshot use the selected destination.
- [x] UI test: Free selected destination can be changed from search.
- [x] UI test: Free blocked destination opens paywall replaced by selected-destination model.
- [x] UI test: Pro destination picker shows all supported destinations through search.
- [x] UI test: migration keeps previous traveler destination through existing `travelerCurrency` state.

### P9.4 Profile-Based Information Density

- [x] Define Home ordering per profile:
  - [x] Traveler: task-first, low numbers.
  - [x] Remittances: recipient amount, provider route, alert.
  - [x] Freelancer: invoice amount, fee/spread, timing.
  - [x] Savings: long-term exposure, alert, watchlist.
  - [x] Crypto holder: market movement, portfolio, alerts.
- [x] Keep tabs stable while changing first-screen priority by profile.
- [x] Make Compare secondary for Traveler unless explicitly opened.
- [x] Add profile-specific return reason copy.
- [x] UI test: Home ordering changes by profile.
- [x] UI test: first CTA per profile routes correctly.
- [x] UI test: changing profile in Settings updates Home after returning.

### P9.5 Upgrade Strategy

- [x] Add global floating Pro CTA for Free users.
- [x] Hide global Pro CTA for Pro users.
- [x] Use a visually distinct Pro color treatment for upgrade cards.
- [x] Reduce repeated inline upsells above primary Traveler content.
- [x] Add paywall source labels for:
  - [x] `global_pro_cta`.
  - [x] `traveler_destination_lock` replaced by selected Free destination model.
  - [x] `ocr_lock`.
  - [x] `provider_lock`.
  - [x] `alert_limit`.
  - [x] `traveler_cost_template_lock`.
  - [x] `converter_currency_limit`.
  - [x] `timing_lock`.
  - [x] `remittance_plan_lock`.
  - [x] `transfer_intent_lock`.
- [x] Ensure Free feels useful before the paywall:
  - [x] selected destination.
  - [x] basic local rules.
  - [x] manual converter.
  - [x] limited alert/check.
- [x] Ensure Pro value is concrete:
  - [x] OCR/live scanner.
  - [x] all destinations.
  - [x] offline packs.
  - [x] provider comparison.
  - [x] unlimited alerts.
  - [x] multiple trips/watchlists.
- [x] UI test: global Pro CTA opens paywall for Free.
- [x] UI test: Pro users do not see Free CTA.
- [x] UI test: locked feature paywall entries are source-labelled.

### P9.6 Closed-Test Polish Items

- [x] Improve News empty state:
  - [x] Explain no stories are available.
  - [x] Add refresh guidance.
  - [x] Keep the card full width.
  - [x] Avoid unlabelled sample/fake news.
- [x] Improve Conversion Decision visibility:
  - [x] Move recommendation above supporting analysis.
  - [x] Keep action buttons visible without deep scroll.
  - [x] Keep explanatory text readable.
- [x] Review secondary text readability:
  - [x] Rate source details.
  - [x] Update timestamps.
  - [x] Market notes.
  - [x] Provider source labels.
- [x] Confirm historical chart ranges are P1 or implement safe range selector.
- [x] Confirm favorite reordering/custom groups are P1 unless needed for P9.0.
- [x] UI test: News empty state is full-width and useful.
- [x] UI test: Converter first viewport includes conversion recommendation.
- [ ] UI test: small-phone metadata text does not clip.

### P9.7 Manual QA Before Versioning

- [x] Fresh install as Free.
- [x] Choose Traveler in onboarding.
- [x] Confirm Home shows actions before dense market data.
- [x] Open Tipping/local rules in one tap.
- [x] Change destination to a real tester destination.
- [x] Try a non-selected destination and verify paywall path replaced by selected-destination Free model.
- [x] Tap every visible card in the first two Home viewports and verify action/static behavior.
- [x] Open Compare and confirm it does not feel required for Traveler.
- [x] Open global Pro CTA and confirm paywall copy is clear.
- [ ] Grant Pro in RevenueCat or use a test purchase.
- [ ] Reopen app and confirm Pro removes Free limits without changing saved trip data.
- [x] Run full Android instrumentation suite on connected Android test device.
- [x] Run Firebase Test Lab matrix.
- [x] Confirm no new hardcoded user-facing strings.
- [x] Prepare release notes around traveler clarity and easier Pro discovery.
- [ ] Confirm Android production gate is met:
  - [x] P9.0 complete.
  - [x] P9.1 complete.
  - [x] P9.2 complete.
  - [x] P9.3 complete or explicitly deferred with rationale.
  - [x] P9.5 complete.
  - [x] P9.6 complete.
  - [x] Connected Android full UI suite green.
  - [x] Firebase Test Lab green.
  - [x] Free manual QA complete.
  - [ ] Pro manual QA complete.

## Android Launch Readiness

- [x] App icon is configured with padding.
- [x] Bottom navigation uses 5 primary destinations.
- [x] Android local notification channel is configured.
- [x] Android alert worker checks active alerts against backend rates.
- [x] Android widgets are implemented for rates and traveler.
- [x] Move notification permission request to a contextual moment.
- [x] Confirm widgets refresh correctly after fresh install, app kill and backend cache refresh.
- [x] Validate offline/slow-network first launch on a clean install.

## iOS Launch Readiness

- [x] Shared Compose code compiles for iOS arm64.
- [x] RevenueCat KMP gateway exists for iOS.
- [ ] Create production iOS Firebase app and add `GoogleService-Info.plist`.
- [ ] Enable Sign in with Apple in Apple Developer and Firebase Auth.
- [ ] Replace RevenueCat Test Store key with App Store public SDK key.
- [ ] Add iOS local notification implementation.
- [x] Explicitly defer iOS widgets until after Android internal testing.
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
- [x] Shows an explicit conversion decision card with timing, best route and alert action.
- [x] Shows a provider recommendation card before the Provider Matrix.
- [x] Remittance planner supports simple recipient profiles.
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
- [x] Alerts screen states backend/FCM monitoring and Android local fallback clearly.
- [x] Supports Above/Below target-rate alerts.
- [x] Supports Daily Move alert type.
- [x] Supports quick presets: -1%, -0.5%, +0.5%, +1%.
- [x] Debug test notification action exists per alert.
- [x] Alerts keep their original base pair when app base currency changes.
- [x] Duplicate identical alerts reactivate the existing alert.
- [x] Add triggered-alert history.
- [x] Add server-side alert evaluation for more reliable background delivery.
- [x] Server-side alert evaluation sends FCM push notifications to registered Android devices.
- [x] Android keeps the local alert worker as a fallback path.
- [x] Alert notification copy is localized for local Android alerts and server FCM pushes.
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
- [x] Provider preferences exist in Settings with market-aware main providers and selectable other markets.
- [x] Provider preferences sync through Firebase backup.
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
- [x] Watchlist groups explain why each tracked bucket matters.
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

## Provider Network

- [x] Provider catalog includes global transfer providers, LatAm wallets/rails, digital-dollar routes and local rails.
- [x] Australia/Oceania users see local main providers first while LatAm providers remain selectable.
- [x] Converter provider comparison respects the selected provider preferences.
- [x] Firebase Functions exposes a provider catalog endpoint for server-side catalog/crons.
- [x] Integrate official Wise quote API behind Firebase Functions with backend fallback to explicit estimates.
- [x] Integrate Wise Comparison API behind Firebase Functions for market comparison data across supported providers.
- [x] Integrate MoneyGram quote API path behind Firebase Functions, gated by partner credentials.
- [x] Add provider credential/affiliate configuration without exposing secrets in the app.
- [x] Add server-side quote freshness, quote source labels and fallback policy per provider.
- [x] Provider Matrix copy distinguishes live provider quotes, market comparisons, backend estimates, partner setup and unsupported routes.
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
- [x] Home shows a profile-aware best next action with convert, alert and provider comparison actions.
- [x] Home shows a daily money brief with watch target, proof-of-value and return reason.
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
- [x] Launch smoke test verifies no `COMING NEXT` placeholder remains in the main flow.

## Release Hardening Evidence

- [x] 2026-05-22: Firebase `evaluateServerAlerts` scheduled function deployed in `fx-always`.
- [x] 2026-05-22: Firebase `providerQuotes` and `refreshProviderQuoteCache` deployed with provider quote cache `v6`.
- [x] 2026-05-22: Firebase `evaluateServerAlerts` redeployed with FCM push fanout for server alert events.
- [x] 2026-05-22: S25 reinstall verified Android FCM token registration via `FxPushToken`.
- [x] 2026-05-22: Alert notification i18n added for all 13 supported languages; `evaluateServerAlerts` redeployed.
- [x] 2026-05-22: Provider status/source copy verified across all 13 app languages for live, comparison, estimate, partner setup and unavailable states.
- [x] 2026-05-22: Android full instrumentation suite passed on S25: 124/124 tests.
- [x] 2026-05-22: Clean-install offline first launch on S25 produced no app crash.
- [x] 2026-05-22: App-kill/startup widget refresh path on S25 produced no widget/RemoteViews crash.

## Recommended Next Work

1. [x] P0: strengthen Convert with an explicit fee/spread reality check card.
2. [x] P0: expand smart alert suggestions and add triggered-alert history.
3. [x] P0: turn Traveler into an offline trip pack with local card/ATM/tipping guidance.
4. [x] P0: add watchlist groups for Travel, Family, Savings, Work and Crypto.
5. [x] P0: expand Android widgets with cached-age status and separate widget entry intents.
6. [x] P1: harden remittance planner in Convert with family route, recurring amount, cadence, recipient estimate, next-send window, confidence and annual fee drag.
7. [x] P1: add economic calendar light by currency, impact level, Pro filters and calendar plan.
8. [x] P1: improve portfolio exposure with concentration, scenario impact, digest and action plan.
9. [x] P1: add shareable rate cards with source, timestamp and disclaimer.
10. [x] P1: add daily/weekly notification digest with next reminder and cadence-specific summary.
11. [x] P2: add manual/local rate notebook for official vs informal markets.
12. [x] P2: add city/country travel cost templates.
13. [x] P2: add provider comparison history by route and amount.
14. [x] P3: add release readiness panel for internal testers.
15. [x] P3: add support snapshot copy for tester reports.
16. [x] P4: add internal testing manual QA checklist.
17. [x] P4: add copyable internal test plan for tester distribution.
18. [x] P5: add store listing kit with release copy and disclaimer.
19. [x] P5: add copyable store listing draft for release prep.
20. [x] P6: add Android OCR price scanner with local-vs-live hidden cost check.
21. [x] P6: add widget quick setup for rates pair and traveler destination.
22. [x] P6: harden Android widget layout rendering for Samsung/RemoteViews.
23. [x] P7: add commercial decision layer to Home, Convert, Provider Matrix and Alerts.
24. [x] P8: add retention loop brief and watchlist reasons.
25. Release: close legal content, store listing, release signing and full instrumentation run.
