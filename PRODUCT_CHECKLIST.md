# FX Always Product Checklist

Use this as the production readiness checklist for each feature. Mark items only after testing on Android and sanity-checking KMP/iOS compile.

## Global

- [x] Firebase backend URL points to `moneytrackerpro-8ff64`.
- [x] App supports theme mode and base currency settings.
- [x] UI supports English, Spanish, Portuguese and additional popular languages.
- [x] Android app icon is configured with padding.
- [x] Bottom nav uses 5 primary destinations.
- [x] Hide debug-only subscription toggle outside debug builds.
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
- [x] Amount input is editable.
- [x] Add reverse pair action.
- [x] Free users can edit a limited converter currency list.
- [x] Pro users can use every supported converter currency.
- [ ] Validate decimal separators for locales using comma.
- [ ] Handle crypto precision without rounding important digits.
- [ ] Persist the last converter amount.

## Compare

- [x] Uses live rates from selected base currency.
- [x] Free users see limited comparison set.
- [x] Overlay card no longer covers the currency grid.
- [x] Add pair selection/edit list.
- [ ] Make overlay series reflect selected currencies.
- [x] Add empty state if compare list is unavailable.
- [ ] Visually verify Free limit and Pro unlimited compare list on Android and iOS.

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
- [x] Prevent duplicate identical alerts by reactivating the existing alert.
- [ ] Verify upgrade path when user hits the Free alert limit.

## News

- [x] News feed comes from Firebase backend.
- [x] News is gated for Free/Pro story count.
- [x] Backend strategy supports language, region and currencies.
- [x] Add user-facing region selector.
- [x] Add currency filters.
- [x] Open article/source links when the provider gives a URL.
- [x] Add empty state for region with no relevant stories.
- [x] Add cache age and refresh feedback.
- [x] Add topic filters.

## Traveler

- [x] Shows local cheat sheet and etiquette card.
- [x] Free/Pro cheat sheet gating exists.
- [ ] Add actual location/country detection with permission flow.
- [x] Add manual destination selector.
- [x] Persist last travel destination offline.
- [ ] Add country-specific ATM/card/tipping data source.
- [ ] Handle countries with multiple accepted currencies.

## Settings

- [x] Theme mode persists.
- [x] Base currency persists and updates app rates.
- [x] Version name is visible.
- [x] Restore purchase action calls RevenueCat restore.
- [x] Debug dev premium toggle exists.
- [x] Remove or hide dev toggle in release builds.
- [x] Add notification permission/status row.
- [x] Add language selector.
- [x] Add legal/privacy links.
- [x] Add account/subscription management once RevenueCat is live.

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
- [x] Connect RevenueCat Android through KMP gateway.
- [x] Connect RevenueCat iOS through KMP gateway.
- [x] Add entitlement validation for `pro`.
- [x] Add restore purchase behavior.
- [x] Add purchase/restore in-progress state to avoid duplicate taps.
- [ ] Add manage subscription deep links.
- [ ] Add paywall A/B-ready copy and pricing.
- [ ] Test offline entitlement cache.

## Free/Pro Use Case Audit

### Converter

- [x] Free: user can convert with the base currency and a limited target list.
- [x] Free: extra converter currencies open the paywall instead of silently failing.
- [x] Pro: user can select from all supported backend currencies.
- [x] Pro: full fee provider list is visible.
- [ ] Edge: selected target removed from backend falls back to a valid currency.

### Compare

- [x] Free: comparison board is limited by `compareLimit`.
- [x] Free: edit list blocks extra currencies with a Pro affordance.
- [x] Pro: edit list accepts the complete supported currency set.
- [x] Edge: empty compare data shows a useful state instead of an empty board.

### Details

- [x] Free: 1D, 1W and 1M history load.
- [x] Free: 1Y and ALL open the paywall.
- [x] Pro: 1Y and ALL request backend history.
- [x] Pro-loading: cached chart remains visible while the new period loads.
- [ ] Edge: backend history error keeps the last useful chart and shows a non-blocking error.

### Alerts

- [x] Free: one alert can be created.
- [x] Free: second alert opens the paywall.
- [x] Pro: unlimited app-side alert creation is allowed.
- [x] Edge: alerts keep the original pair when base currency changes.
- [x] Edge: duplicate identical alerts reactivate the existing alert instead of adding another row.

### Watchlist

- [x] Free: limited tracked currencies.
- [x] Free: locked rows show a Pro action when the limit is reached.
- [x] Pro: unlimited tracked currencies.
- [x] Edge: base currency can be tracked at rate `1.0`.
- [ ] Edge: removing a tracked currency clears or preserves its holding by explicit product decision.

### News

- [x] Free: limited story count and locked filters.
- [x] Pro: region and currency filters are enabled.
- [x] Pro: full filtered story list is visible.
- [x] Edge: no stories for a selected region/currency shows a specific empty state.

### Traveler

- [x] Free: local destination list is limited.
- [x] Free: More destinations opens the paywall.
- [x] Pro: full destination picker is enabled.
- [x] Edge: selected destination stays visible even if it is not in the default popular set.

### Settings And Restore

- [x] Free/Pro state comes from RevenueCat entitlement when configured.
- [x] Restore purchase updates the visible subscription state.
- [x] Production builds hide the local dev override.
- [ ] Edge: offline restore failure copy is clear and does not change entitlement state.
