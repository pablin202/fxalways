# Store Internal Testing Plan

Use this before Android internal testing, Android closed testing, and iOS TestFlight.

## Android Internal Testing

1. RevenueCat
   - Use the Android public SDK key, not the Test Store key.
   - Entitlement: `pro`.
   - Offering: `pro` recommended. The app also accepts `default` or the current offering as fallback.
   - Packages:
     - `$rc_monthly` -> `fxalways_pro_monthly`
     - `$rc_annual` -> `fxalways_pro_annual`
   - Do not configure or expose a lifetime package.

2. Google Play Console
   - Create subscriptions or base plans for `fxalways_pro_monthly` and `fxalways_pro_annual`.
   - Add tester emails under license testing for billing sandbox checks.
   - Upload a signed release AAB to Internal testing.
   - Share the opt-in link with testers.

3. Tester checklist
   - Install from the Play internal testing link.
   - Open Home and confirm rate source, updated time, cached/live state and disclaimer are visible.
   - Open Convert and confirm the same rate trust block is visible.
   - Open Paywall and confirm only Monthly and Yearly appear.
   - Buy Monthly with a test payment method and confirm Pro unlocks.
   - Restore purchase and confirm Pro remains active.
   - Try offline/slow network and confirm cached/loading states are understandable.
   - Add one alert, one crypto, one holding and one widget.

## iOS TestFlight

1. App Store Connect
   - Create monthly and annual subscriptions only.
   - Use the iOS public SDK key in the iOS build configuration.
   - Add the products to the same RevenueCat `pro` entitlement and `pro` offering.

2. TestFlight
   - Upload the iOS build.
   - Add internal testers.
   - Validate Monthly, Yearly, Restore and sandbox renewal/expiration behavior.

## Store Screenshot Set

Prepare screenshots for these exact release claims:

- Live FX + crypto rates with visible source and updated time.
- Converter with rate trust, mid-market value and fee comparison preview.
- Rate alerts and smart alert suggestions.
- Traveler mode with local budget and offline-safe rates.
- Portfolio/watchlist with holdings and P&L.
- Paywall showing Monthly and Yearly only.

Avoid claiming bank-grade execution or live transfer pricing. FX Always shows indicative rates, alerts, comparison context and portfolio/travel tools; it does not move money.
