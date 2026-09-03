# FX Always Product Benchmark And Roadmap

Last updated: 2026-06-21

## Positioning

FX Always should not compete as a bank, broker or remittance processor. That path requires custody, KYC, payments, licensing, dispute handling and heavy regulatory work.

The stronger position is:

> FX intelligence, travel money and remittance decision support for people who need to know when to convert, what a conversion really costs and what to watch next.

This keeps the product useful in developed markets and emerging markets without taking regulated money movement into the app.

## Benchmark Set

- Wise: transparent transfer pricing, rate alerts, multi-currency account, repeat transfers and Apple Pay / Google Pay funding.
- Revolut: travel FX, budgeting, analytics, multi-currency balances, spending controls and a broad financial super-app model.
- XE: currency converter, charts, mid-market rate alerts and offline last-rate behavior.
- WorldRemit / Western Union / Remitly: remittance flows, cash pickup, mobile money, airtime top-up, repeat sends, tracking and payout-method choice.
- TradingView / OANDA / Bloomberg: watchlists, alerts, charts, macro calendars, market news and security-level monitoring.
- Travel/offline converter apps: offline conversion, location-aware destination currency, widgets and low-friction traveler workflows.

## Product Principles

- Be a decision layer, not a transfer provider.
- Show mid-market truth first, then estimate the real-world cost.
- Make time-to-convert understandable without trader complexity.
- Treat offline, low-data and slow-network states as primary product states.
- Build for both first-world use cases and emerging-market constraints.
- Keep Pro value in depth, automation and personalization, not basic access.

## Market Segments

### Developed Markets

Primary users:
- Travelers using cards, ATMs and foreign subscriptions.
- Freelancers paid in USD, EUR, GBP or crypto.
- Expats with savings and recurring expenses in multiple currencies.
- Investors with foreign cash or portfolio exposure.

Most valuable features:
- Fee/spread reality check.
- Smart conversion timing.
- Multi-currency portfolio exposure.
- Widgets and quick actions.
- Economic event reminders.
- Advanced watchlists and alerts.

### Emerging Markets

Primary users:
- People receiving or sending family support.
- Users comparing official, bank, card, mobile wallet and cash pickup contexts.
- Travelers or workers dealing with weak connectivity.
- Users tracking inflation/devaluation exposure.

Most valuable features:
- Remittance planner.
- Offline-first rates and traveler packs.
- Manual/local rate notebook.
- Low-data mode.
- Cash pickup/mobile wallet categories.
- Clear source, timestamp and trust labels.

## P0: Implemented In Current Iteration

These items should move FX Always from a polished converter into a product that saves money and reduces uncertainty.

1. Fee/spread reality check - implemented and UI-tested
   - Show what the recipient gets after estimated fee and markup.
   - Show loss vs mid-market in target currency and percent.
   - Flag good, acceptable, expensive and avoid routes.
   - Keep the free version useful with mid-market + custom cost; use Pro for the full provider list.

2. Smart alerts - implemented and UI-tested
   - Keep current target-rate and daily-move alerts.
   - Add suggested alerts based on recent range: near 30d high/low, better-than-average and volatility.
   - Make suggested alert creation one tap where possible.
   - Add triggered-alert history.

3. Travel offline pack - implemented and UI-tested
   - Make last-rate offline state explicit.
   - Add trip budget, daily budget, cash buffer and local price guide as a saved trip pack.
   - Add card/ATM/tipping guidance by destination.
   - Add "pay in local currency" guidance to avoid DCC.

4. Watchlist groups - implemented and UI-tested
   - Add groups such as Travel, Family, Savings, Work and Crypto.
   - Surface tracked and suggested codes inside each group.
   - Defer editable per-currency reasons to P1 to avoid a watchlist storage migration in this release.

5. Android widget expansion - implemented and parser-tested
   - Keep current traveler/rate widgets.
   - Add cached-age status to the rates widget.
   - Widget intents deep-link into Today, Convert, Send, Alerts, Watchlist and Traveler (issue #12).
   - Later: iOS widget extension.

## Rate Freshness Policy (issue #3, 2026-09-03)

What the app shows and how it is labelled, so the UI never implies more granularity than the data has.

| Data | Source | Real cadence | Label in the app |
|---|---|---|---|
| FX mid-market | Frankfurter (ECB reference rates) | One fixing per business day (~16:00 CET); backend cache refreshes hourly | `DAILY REFERENCE · Rate of 3 Sep · synced 14:05`. Changes are "vs yesterday". Ranges and sparklines are "30D". |
| Crypto | CoinPaprika | Backend cache every 10 minutes | "24H" labels are legitimate here (CoinPaprika reports a rolling 24h change). |
| Provider quotes | Wise live quote, Wise Comparison, estimates | Live on request, cached 15 min | Status per provider: live / comparison / estimated / partner setup (unchanged). |
| Offline | Local cache | Whatever was last synced | `OFFLINE · last rate 2 Sep · saved 3h ago`. |

Rules:
- No `LIVE`, `1H`, `24H` or `VOLATILITY` copy on FX surfaces. The pulsing live dot is gone from Home.
- Every FX number carries a reference date; the sync time says when *this device* fetched it, never when the market moved.
- The hero range and sparkline use the same 30-calendar-day window (`SPARKLINE_WINDOW_DAYS`).
- Home tiles are computed from data (top mover vs yesterday, pinned 30D range). No hardcoded values.

Intraday spike (timeboxed, decision):
- Candidates: exchangerate.host (free tier, 1h refresh, unreliable uptime), Open Exchange Rates (US$12/mo for 1h, hourly not tick), Wise quote endpoint (already integrated in `providerQuotes`, true live mid-market per corridor, rate-limited).
- Decision: keep the ECB daily reference as the free, universal baseline and **use the Wise live quote as the "live" number only inside the send/convert decision for the user's corridor** (Phase 2, "Decision of the day"). That is where freshness changes a decision; a dashboard of 150 currencies ticking hourly does not.
- Revisit if Phase 2 analytics show users bouncing on the reference date, or if a corridor needs sub-daily alerts.

## P1

- Remittance planner with recurring amount, family route, reminder cadence, recipient estimate, next-send window, route confidence and annual fee drag. Hardened in Convert and UI-tested.
- Economic calendar light by currency and impact level, with Pro impact filters and calendar plan. Hardened in Detail and UI-tested.
- Portfolio exposure improvements: currency concentration, scenario impact, daily digest and action plan. Hardened in Watchlist and UI-tested.
- Shareable rate cards with source, timestamp and disclaimer. Started in Detail and UI-tested.
- Daily/weekly notification digest with next reminder and cadence-specific summary. Hardened in Alerts and UI-tested.

## P2

- Manual/local rate notebook for markets with official vs informal rates. Started in Convert and UI-tested.
- City/country travel cost templates. Started in Traveler and UI-tested.
- Provider comparison history by route and amount. Started in Convert and UI-tested.
- Provider preferences by market: Settings now separates local/main providers from other markets, lets users select LatAm wallets/rails even when their base is Australia, syncs those choices through Firebase backup, and filters Convert provider comparisons/transfer intent to the selected quote-capable providers. UI-tested.
- Firebase provider catalog endpoint: Functions exposes `providerCatalog?base=...` with primary/other grouping, quote mode and market metadata so the static in-app catalog can move server-side without changing the UX.
- Provider quote backend: Functions now exposes `providerQuotes` with freshness, source labels, Free/Pro provider caps, Wise live quote support, Wise Comparison market data for supported providers, Revolut/MoneyGram credential-gated adapters and explicit estimated/partner-setup fallbacks. Provider Matrix copy now separates live provider quotes, market comparisons, backend estimates, partner setup and unsupported routes. Venmo, PayPay, Mercado Pago, Nequi, Yape, Pix and PicPay remain wallet/local rails until direct quote APIs or partnerships are available.
- OCR price/receipt converter.
- More explicit family/remittance workflows.

## Release Hardening

- Server-side alert evaluation runs every 15 minutes through Firebase Functions, updates backed-up alert snapshots when target-rate or daily-change alerts trigger and sends FCM push notifications to registered Android devices.
- Android keeps the local alert worker as a fallback when device-side scheduling is available.
- Provider quote cache `v6` invalidates old production copy so users see the transparent provider status labels immediately.
- Android release QA for this pass covered full instrumentation, clean offline first launch, startup widget refresh and S25 reinstall.

## P3

- Release readiness panel for internal testers with build, backup and legal status. Started in Settings and UI-tested.
- Support snapshot copy for tester reports. Started in Settings and UI-tested.

## P4

- Internal testing manual QA checklist with Free, Pro, offline/cache and paywall/legal coverage. Started in Settings and UI-tested.
- Copyable internal test plan for tester distribution. Started in Settings and UI-tested.

## P5

- Store listing kit with title, short description, keywords and disclaimer. Started in Settings and UI-tested.
- Copyable store listing draft for release prep. Started in Settings and UI-tested.

## P6

- Market competitiveness pass for Android against XE, Wise, Revolut, Currency Converter Plus, MyCurrency and Currency.Wiki.
- OCR price scanner in Convert on Android: Pro users can scan a shelf/cash-desk price with the camera, ML Kit extracts the amount, and the existing live-vs-local hidden cost check updates automatically. Free keeps manual entry plus upsell. UI-tested for Free/Pro entry points.
- Widget quick setup in Settings: users can pin the rates widget pair and traveler widget destination, then refresh Android widgets immediately. UI-tested.
- Widget rendering hardening for Samsung/RemoteViews: solid backgrounds, explicit padding and safe widget view classes.

## P7

- Commercial productization pass against Wise, XE, Revolut, WorldRemit/Remitly-style remittance flows and emerging-market wallet behavior.
- Home now surfaces one profile-aware "best next action" so the product feels like a decision assistant instead of only a rates board.
- Convert now starts from a decision card: amount, timing signal, best route, provider trust state and alert action are visible before deeper analysis.
- Provider Matrix now has a recommendation layer above the table with best value, quote completeness, live/comparison/partner/estimate status and Pro unlock copy.
- Alerts now explain production monitoring honestly: backend evaluation with FCM delivery where available, plus Android local checks as fallback.
- Remittance planning now includes simple recipient profiles so repeat family, client, savings and trip flows are easier to understand.
- Store screenshot claim set should lead with: "Know when to convert", "Compare real provider cost", "Scan prices abroad", "Get push alerts", "Track your multi-currency money".

## P8

- Retention loop pass inside the product, focused on activation, habit and perceived savings rather than store copy.
- Home now includes a daily money brief with watch target, proof-of-value and a profile-specific return reason.
- Watchlist groups now explain why each bucket exists, so tracked currencies feel intentional instead of arbitrary.
- The loop strategy is: profile -> daily brief -> best action -> alert/watch/provider decision -> return trigger.

## P9: Tester Feedback Product Reset

Goal: align the in-app experience with the Play Store/onboarding promise for regular travelers while keeping advanced FX workflows available for users who explicitly choose them.

Primary tester feedback driving this phase:
- The app store and intro feel like a regular traveler product, but the app still shows too many numbers and advanced FX concepts too early.
- Tipping culture and local travel guidance are promised, but not easy enough to find.
- Some UI surfaces look clickable but do not perform an action.
- Compare creates frustration when the user expects a simple travel workflow.
- Free countries/destinations should be user-selected, not a fixed list that may exclude a tester's actual trip.
- Upgrade prompts are visible, but the actual upgrade path must stay obvious and visually distinct.

Closed-testing report summary, 14-day cycle ending 2026-06-20:
- Overall rating: 4.6/5.
- Stability: no crashes, freezes or major performance issues observed.
- Strongest positives: multi-currency conversion, Traveler utilities, Compare, Watchlists, Alerts, News integration, Crypto monitoring and offline cached rates.
- Repeated improvement themes: first-time guidance, information density, Pro banner frequency, historical chart ranges, favorite currency management, news personalization, richer travel planning, smaller secondary text and weak News empty state.
- Production implication: no technical blocker was identified, but Android production should wait until the core clarity issues below are completed because they directly affect activation and retention.

### Closed-Test Production Gate

Android production is considered ready after:
1. P9.0 Traveler Home Simple is implemented and UI-tested.
2. P9.1 Tipping and Local Rules are one-tap visible from Traveler Home.
3. P9.2 Clickability Contract audit is completed for Home, Traveler, Convert, Compare, More and Settings.
4. P9.3 Free destination choice is implemented or explicitly deferred with product rationale.
5. P9.5 upgrade surfaces are cleaned up so Pro remains easy to find without obstructing core information.
6. News empty state and Conversion Decision visibility are improved.
7. Small secondary text is reviewed on S25 and a small-phone viewport.
8. Full Android instrumentation passes on S25.
9. Firebase Test Lab passes on the configured phone/tablet matrix.
10. Manual QA is completed for Free and Pro.

### P9.0: Traveler Home Simple

Problem: Traveler users should not land on a trader-style rates board.

Implementation steps:
1. Add a Traveler-specific home layout branch inside the existing dashboard route.
2. Move four primary action tiles above market data:
   - Check a price.
   - Convert money.
   - Tipping and local rules.
   - Trip budget.
3. Keep the main rate card, but move detailed market movement and compare-heavy content below the primary actions.
4. Replace technical subtitles with task-based copy:
   - "Will this cost more than expected?"
   - "How much is this in my money?"
   - "Should I pay in local currency?"
5. Keep advanced charts and market detail available through Detail/Compare, not as the first Traveler impression.

Free behavior:
- 1 active trip.
- 1 selected destination.
- Basic local rules.
- Manual price check.
- Manual converter.

Pro behavior:
- Multiple trips.
- Offline country packs.
- OCR/live price scanner.
- Full local rules and cost templates.
- Unlimited destinations.

Acceptance criteria:
- A Traveler user can identify the three most relevant actions within 5 seconds of Home loading.
- Tipping/local rules are visible above the fold on common phone sizes.
- Home still exposes a clear rate, but not more than one dense market block before the first action tiles.
- Free users see one clear Pro entry point without repeated upsell cards stacking above the first task.

UI tests:
- Traveler Home renders action tiles before market detail.
- Each Traveler action tile is clickable and routes to the expected screen/section.
- Free and Pro render different limits without changing tile order.
- Small-phone screenshot/layout test verifies no clipped action copy.

### P9.1: Tipping And Local Rules As A Core Feature

Problem: local travel guidance is a promised feature but currently feels secondary.

Implementation steps:
1. Promote local rules to a first-class Traveler section and Home tile.
2. Structure each destination guide into:
   - Tipping.
   - Card acceptance.
   - Cash needed.
   - ATM warning.
   - Dynamic currency conversion warning.
   - Local price norms.
3. Add a compact "before you pay" card for the current destination:
   - Pay in local currency.
   - Avoid airport cash unless urgent.
   - Check terminal rate against snapshot.
4. Add data-source metadata: "guide estimate", "cached", "updated".
5. Keep country-level rules in Free and richer city/offline detail in Pro.

Acceptance criteria:
- A user can reach tipping guidance from Home in one tap.
- Guidance uses traveler language, not FX trader language.
- Empty/missing guide states explain what is unavailable and keep the layout full width.
- All copy goes through i18n.

UI tests:
- Traveler Home local rules tile opens Traveler/local rules content.
- Missing destination data shows a full-width empty state.
- Free shows country basics; Pro shows expanded offline/city/cost details.

### P9.2: Clickability Contract

Problem: visually rich cards create frustration if they look tappable but do nothing.

Implementation steps:
1. Audit every `BentoCard`, `BentoTile`, `MetricTile`, `Pill` and row in:
   - Home.
   - Traveler.
   - Convert.
   - Compare.
   - More.
   - Settings.
2. Classify each component:
   - Action: must be clickable, route or mutate state.
   - Detail: must open a detail screen/sheet.
   - Selection: must visibly toggle selected state.
   - Static: must not look like a button and should not show arrows/hover affordance.
3. Add arrows only when the surface navigates.
4. Add disabled styling only when the surface is intentionally unavailable.
5. Add test tags for newly actionable cards.

Acceptance criteria:
- No arrow appears on a non-clickable surface.
- No highlighted chip/card is static unless it clearly reads as status.
- Compare cards that look actionable either open detail or use static visual styling.
- Free locked rows open paywall or show a disabled reason consistently.

UI tests:
- Action cards perform clicks and update route/counter.
- Static informational cards have no click side effects.
- Free locked items open paywall once, without duplicate events.

### P9.3: Free Country/Destination Choice

Problem: fixed free countries feel unfair when the user's real country is not included.

Implementation steps:
1. Replace fixed free destination assumptions with user-selected free destination slots.
2. Let Free choose 1 or 2 destinations depending on final monetization policy.
3. Persist selected free destinations in backup settings.
4. Let Pro select unlimited supported destinations.
5. Update destination picker copy from "Free shows 8" to "Free includes your selected destination".
6. Add migration fallback for existing users:
   - keep current traveler destination as the first free slot.
   - if missing, default from locale/base currency.

Free behavior:
- User chooses destination slot(s).
- Non-selected destinations show preview/paywall.
- Existing selected destination stays available.

Pro behavior:
- All supported destinations unlocked.
- Multiple trip/destination workflows available.

Acceptance criteria:
- A Free tester can pick their actual country if supported.
- Changing selected destination does not break trip budget, widgets or offline snapshot.
- Paywall copy frames Pro as "all destinations and trips", not "your country is blocked".

UI tests:
- Free destination slot can be selected and persists.
- Free blocked destination opens paywall.
- Pro destination picker shows all supported destinations.
- Migration keeps previous traveler currency/destination.

### P9.4: Profile-Based Information Density

Problem: one UI density cannot serve travelers, remittance users, freelancers and savings users equally.

Implementation steps:
1. Define a density policy per profile:
   - Traveler: task-first, low numbers.
   - Remittances: recipient amount, provider route, alert.
   - Freelancer: invoice amount, fee/spread, timing.
   - Savings: long-term exposure, alert, watchlist.
   - Crypto holder: market movement, portfolio, alerts.
2. Apply the policy to Home ordering and copy.
3. Keep tab structure stable; change what each profile sees first.
4. Add profile-specific "why return" card text.
5. Avoid showing Compare as a primary Traveler action unless the user explicitly opens it.

Acceptance criteria:
- Traveler first screen reads like a travel-money assistant.
- Remittance first screen reads like a send-money decision assistant.
- Freelancer first screen reads like invoice/payment protection.
- No profile loses access to existing advanced tools.

UI tests:
- Home ordering differs by profile.
- Each profile's first CTA routes to the intended workflow.
- Profile switching in Settings updates Home ordering after returning.

### P9.5: Upgrade Strategy After Feedback

Problem: upgrade prompts exist, but the product needs a cleaner Free-vs-Pro story.

Implementation steps:
1. Keep the global floating Pro CTA for Free users.
2. Reduce repeated inline upsells above primary task content.
3. Make Free useful before asking for Pro:
   - chosen destination.
   - basic local rules.
   - manual conversion.
   - limited alert/check.
4. Make Pro concrete:
   - OCR/live scanner.
   - all destinations.
   - offline packs.
   - provider comparison.
   - unlimited alerts.
   - multiple trips/watchlists.
5. Add paywall entry analytics source labels:
   - `global_pro_cta`
   - `traveler_destination_lock`
   - `ocr_lock`
   - `provider_lock`
   - `alert_limit`

Acceptance criteria:
- Paywall is reachable within one tap from any locked Pro feature.
- First Traveler viewport has at most one upgrade surface.
- Upgrade button color remains visually distinct from normal orange actions.
- Paywall copy matches the user's selected profile.

UI tests:
- Global Pro CTA opens paywall for Free and is hidden for Pro.
- Locked Traveler destination opens paywall with source label.
- OCR lock opens paywall.
- Pro user does not see Free upgrade CTA.

### P9.6: Closed-Test Polish Items

Problem: the 14-day closed test found no stability blockers, but it identified several small product issues that should be fixed before Android production because they affect perceived polish.

Implementation steps:
1. Improve News empty state:
   - Explain that no stories are currently available.
   - Add refresh guidance.
   - Keep the empty-state card full width.
   - Avoid sample/fake news in production unless clearly labelled as examples.
2. Improve Conversion Decision visibility:
   - Move the primary recommendation above supporting analysis.
   - Use a compact action row for "Create alert" and "Compare route".
   - Keep explanatory text visible without requiring deep scroll.
3. Review secondary text sizes:
   - Rate source details.
   - Update timestamps.
   - Market notes.
   - Provider source labels.
4. Add historical chart affordance planning:
   - Keep current chart behavior if implementation is risky.
   - Add documented P1 ranges: 7d, 30d, 6m, 1y.
   - Do not block Android production on expanded charts unless existing charts are misleading.
5. Confirm Favorite Currency Management scope:
   - Reordering/custom groups are P1 unless current ordering blocks the Traveler Home Simple work.

Acceptance criteria:
- News empty state feels intentional and actionable.
- Conversion recommendation is visible before secondary details on common phones.
- Secondary metadata is readable on S25 and small-phone UI tests.
- Historical chart ranges and favorite reordering are documented as post-production P1 unless implemented early.

UI tests:
- News empty state renders full-width with refresh guidance.
- Converter first viewport includes the decision recommendation.
- Small-phone layout renders source/timestamp copy without clipping.

### P9.7: Manual QA Script

Run this manually before release:
1. Fresh install as Free.
2. Choose Traveler in onboarding.
3. Confirm Home shows actions before dense market data.
4. Open Tipping/local rules in one tap.
5. Change destination to the tester's real destination.
6. Try a non-selected destination and confirm paywall path is clear.
7. Tap every visible card in the first two Home viewports and confirm action/static behavior is coherent.
8. Open Compare and confirm it does not feel like the required Traveler path.
9. Open global Pro CTA and confirm paywall copy is clear.
10. Grant Pro in RevenueCat or use a test purchase.
11. Reopen app and confirm Pro removes Free limits without changing saved trip data.

Definition of done:
- Android S25 full instrumentation suite passes.
- Firebase Test Lab passes on configured phone/tablet matrix.
- Manual QA script is completed for Free and Pro.
- No new hardcoded user-facing strings outside i18n.
- Release notes describe the change as traveler clarity and easier Pro discovery.
- Production-access questionnaire uses concrete completed changes, not planned-only wording.

Release notes draft:
- Traveler is now simpler to use first: quick actions for price check, conversion, tipping/local rules and trip budget appear before dense market data.
- Free users can choose their own destination instead of being limited to a fixed country list.
- Pro is easier to find and clearer: one global upgrade entry point, fewer repeated upsells and source-labelled locked features.
- News and conversion screens now have clearer empty states, metadata labels and first-screen recommendations.
- Local travel guidance now includes tipping, cards, cash needs, DCC warnings and local price estimates.

## Immediate Implementation Order

1. P9.0: implement Traveler Home Simple.
2. P9.1: promote Tipping and Local Rules as a one-tap Traveler feature.
3. P9.2: complete Clickability Contract audit and fixes.
4. P9.6: fix News empty state, Conversion Decision visibility and secondary text readability.
5. P9.3: let Free users choose their destination/country slots, or explicitly defer if it risks the production date.
6. P9.4: apply profile-based information density.
7. P9.5: clean up upgrade surfaces and source-labelled paywall entries.
8. P9.7: run manual QA, S25 full UI suite and Firebase Test Lab before versioning.

## Post-Android-Production P1

These came from the closed test but should not block Android production after P9 is complete:
- Historical chart ranges: 7d, 30d, 6m, 1y.
- Drag-and-drop favorite currency reordering.
- Custom favorite/watchlist groups beyond the current grouped watchlist behavior.
- News personalization by followed currencies and crypto assets.
- Richer Trip Workspace: cost-of-living comparisons, local transport estimates and emergency currency information.
- First-time interactive tutorial/tooltips if P9's simpler Traveler Home still shows onboarding friction in analytics.
