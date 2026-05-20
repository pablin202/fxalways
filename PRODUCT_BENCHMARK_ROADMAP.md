# FX Always Product Benchmark And Roadmap

Last updated: 2026-05-20

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
   - Add separate widget intents for Rates, Convert and Watchlist entry points.
   - Later: iOS widget extension.

## P1

- Remittance planner with recurring amount, family route, reminder cadence and recipient estimate. Started in Convert and UI-tested.
- Economic calendar light by currency and impact level. Started in Detail and UI-tested.
- Portfolio exposure improvements: currency concentration, scenario impact and daily digest. Started in Watchlist and UI-tested.
- Shareable rate cards with source, timestamp and disclaimer. Started in Detail and UI-tested.
- Daily/weekly notification digest. Started in Alerts and UI-tested.

## P2

- Manual/local rate notebook for markets with official vs informal rates. Started in Convert and UI-tested.
- City/country travel cost templates. Started in Traveler and UI-tested.
- Provider comparison history by route and amount. Started in Convert and UI-tested.
- OCR price/receipt converter.
- More explicit family/remittance workflows.

## Immediate Implementation Order

1. Strengthen Convert with an explicit fee/spread reality check card.
2. Expand smart alert suggestions and add alert history.
3. Improve Traveler into an offline trip pack.
4. Add grouped watchlists.
5. Expand widgets after the core data model is stable.
