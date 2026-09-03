# Analytics (Firebase Analytics via `Observability`)

All events go through `Observability.event(name, params)` (module `observability`), which forwards to
Firebase Analytics on Android and is a no-op on iOS until the SDK is wired. User properties go through
`Observability.setUserProperty`. Event and parameter names are `snake_case`, values are short strings
(no exact amounts, no free text).

## Activation funnel (issue #5)

| Step | Event | Params | Where |
|---|---|---|---|
| 1 | `onboarding_complete` | `profile` | Onboarding finish |
| 2 | `first_conversion` (once per install) | `base`, `target` | Converter, first non-zero amount typed (`trackFirstConversion`) |
| 3 | `send_decision_viewed` | `base`, `target`, `signal` (`strong_rate` / `good_time` / `wait`), `plan` | `ConversionDecisionCard` shown or pair/signal changes |
| 4 | `provider_compare_viewed` | `base`, `target`, `amount_bucket`, `plan` | `ProviderMatrixCard` with at least one quote |
| 5 | `alert_created` | pair / type | Alert saved |
| 6 | `alert_triggered_opened` | `source` (`push` / `local`) | `MainActivity` when opened from an alert notification |
| 7 | `paywall_opened` → `purchase_started` → `purchase_success` | `source`, `plan`/`product` | Paywall |

`amount_bucket` values: `lt_100`, `100_1k`, `1k_10k`, `gte_10k`.

Retention proxies: `rates_refresh`, `tab_selected`, `widget_used{source}`, `news_story_opened`.

## User properties

| Property | Values | Set in |
|---|---|---|
| `plan` | `free` / `pro` | `FxAppUserTrackingEffect` |
| `premium` | `true` / `false` (legacy, kept for existing reports) | same |
| `profile` | `traveler` / `cryptoholder` / `remittances` / … (`UserProfile.name.lowercase()`) | same |
| `base_currency` | ISO code | same |

## Other events already emitted

`alert_action_convert`, `alert_action_next_alert`, `alert_action_share`, `base_currency_changed`,
`compare_currencies_changed`, `converter_currencies_changed`, `currency_added`, `currency_detail_opened`,
`dashboard_crypto_see_all`, `language_changed`, `more_route_opened`, `news_refresh`, `plan_selected`,
`price_check_copied`, `price_ocr_*`, `profile_*`, `provider_matrix_focused`, `provider_preferences_changed`,
`purchase_restore_*`, `theme_changed`, `transfer_*`, `traveler_*`, `watchlist_toggle`.

## Verifying on a device

```bash
adb shell setprop debug.firebase.analytics.app com.fxalways.app
adb logcat -s FA FA-SVC | grep -E "Logging event|user property"
```

Then open the app, type an amount in Converter and check `first_conversion`, `send_decision_viewed`,
`provider_compare_viewed`. Events also appear in Firebase console → Analytics → DebugView for the device.
Disable with `adb shell setprop debug.firebase.analytics.app .none.`.

## Dashboards to build in GA4 (manual)

1. Funnel exploration: `onboarding_complete` → `first_conversion` → `send_decision_viewed` → `provider_compare_viewed` → `alert_created` → `purchase_success`, segmented by `profile` and `plan`.
2. Retention: users with `alert_triggered_opened` vs. without (D7/D30).
3. Conversion: `paywall_opened` by `source` → `purchase_success`.
