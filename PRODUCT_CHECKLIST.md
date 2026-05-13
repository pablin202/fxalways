# FX Always Product Checklist

Use this as the production readiness checklist for each feature. Mark items only after testing on Android and sanity-checking KMP/iOS compile.

## Global

- [x] Firebase backend URL points to `moneytrackerpro-8ff64`.
- [x] App supports theme mode and base currency settings.
- [x] UI supports English, Spanish, Portuguese and additional popular languages.
- [x] Android app icon is configured with padding.
- [x] Bottom nav uses 5 primary destinations.
- [ ] Replace debug-only subscription toggle with RevenueCat entitlement checks.
- [ ] Add analytics events for paywall impressions, feature locks and purchase taps.
- [ ] Add privacy policy, terms and store listing text.
- [ ] Validate all strings are localized before store release.

## Rates

- [x] Loads latest rates from Firebase backend.
- [x] Falls back gracefully when backend is unavailable.
- [x] Refresh action is available.
- [x] Base currency changes reload rates.
- [ ] Show explicit loading skeletons for slow networks.
- [ ] Confirm stale/cached timestamp is accurate.
- [ ] Validate unavailable currency codes are hidden or explained.

## Convert

- [x] Uses live rates from selected base currency.
- [x] Fee comparison has Free/Pro gating.
- [ ] Make amount input editable and persistent.
- [ ] Add reverse pair action.
- [ ] Validate decimal separators for locales using comma.
- [ ] Handle crypto precision without rounding important digits.

## Compare

- [x] Uses live rates from selected base currency.
- [x] Free users see limited comparison set.
- [x] Overlay card no longer covers the currency grid.
- [ ] Add pair selection/edit list.
- [ ] Make overlay series reflect selected currencies.
- [ ] Add empty/error state if compare list is unavailable.

## Alerts

- [x] Alerts persist locally on Android/iOS.
- [x] Free users limited to 1 alert; Pro is unlimited.
- [x] Supports Above/Below manual targets.
- [x] Supports quick presets: -1%, -0.5%, +0.5%, +1%.
- [x] Android worker checks active alerts against backend rates.
- [x] Android local notification channel and permission are configured.
- [x] Debug test notification action exists per alert.
- [x] Alerts keep their original base pair when app base currency changes.
- [ ] Move notification permission request to contextual moment.
- [ ] Add triggered-alert history.
- [ ] Add Daily Move alert type.
- [ ] Add server-side alert evaluation for more reliable background delivery.
- [ ] Add iOS local notification implementation.
- [ ] Prevent duplicate identical alerts unless user confirms.

## News

- [x] News feed comes from Firebase backend.
- [x] News is gated for Free/Pro story count.
- [x] Backend strategy supports language, region and currencies.
- [ ] Add user-facing region selector.
- [ ] Add topic/currency filters.
- [ ] Open article/source links.
- [ ] Add empty state for region with no relevant stories.
- [ ] Add cache age and refresh feedback.

## Traveler

- [x] Shows local cheat sheet and etiquette card.
- [x] Free/Pro cheat sheet gating exists.
- [ ] Add actual location/country detection with permission flow.
- [ ] Add manual destination selector.
- [ ] Persist last travel destination offline.
- [ ] Add country-specific ATM/card/tipping data source.
- [ ] Handle countries with multiple accepted currencies.

## Settings

- [x] Theme mode persists.
- [x] Base currency persists and updates app rates.
- [x] Version name is visible.
- [x] Restore purchase action placeholder exists.
- [x] Dev premium toggle exists.
- [ ] Remove or hide dev toggle in release builds.
- [ ] Add notification permission/status row.
- [ ] Add language selector.
- [ ] Add legal/privacy links.
- [ ] Add account/subscription management once RevenueCat is live.

## Watchlist And Portfolio

- [x] Watchlist persists locally on Android/iOS.
- [x] Free users limited to 4 currencies; Pro is unlimited.
- [x] Users can add/remove currencies.
- [x] Tracked rows open detail screen.
- [x] Portfolio holdings persist by currency code.
- [x] Portfolio total recalculates when base currency changes.
- [x] Portfolio supports holding in the current base currency at rate 1.0.
- [ ] Add multiple watchlists for Pro.
- [ ] Add manual rename for watchlist.
- [ ] Add allocation percentages.
- [ ] Add daily portfolio move estimate.
- [ ] Add import/export CSV.
- [ ] Confirm holdings behave correctly when a currency is removed from watchlist.

## Monetization

- [x] Free/Pro policy is centralized.
- [x] Gating exists for alerts, watchlist, compare, news, traveler, fees and base currencies.
- [ ] Connect RevenueCat Android.
- [ ] Connect RevenueCat iOS.
- [ ] Add product IDs and entitlement validation.
- [ ] Add restore/manage subscription behavior.
- [ ] Add paywall A/B-ready copy and pricing.
- [ ] Test offline entitlement cache.
