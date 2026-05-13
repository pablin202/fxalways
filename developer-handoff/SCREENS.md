# Screens

For every screen: the layout tree, the copy, the states, and what's hooked up to data vs static. JSX file references are canonical — open them alongside this doc.

---

## 1 · Onboarding   (`screens-core.jsx` → `OnboardingScreen`)

Three swipeable steps. **No skip button needed** for testing — show the 3 steps in dev mode behind a debug switch. Production: first launch only, then never again.

### Layout (single step)

```
┌─ STATUS BAR ─────────────────────────────┐
│ FX/                              Skip    │
│                                          │
│                                          │
│        ╭───────────────╮                 │
│       (   grid bg     )                  │
│        │       ⌖       │                 │  ← rotating mono glyph
│        │   (200x200)   │                 │     ⌖  step 1
│         ╰─────────────╯                  │     ⬢  step 2
│                                          │     ◐  step 3
│                                          │
│ STEP 01 · LIVE RATES                     │
│ Every rate.                              │
│ Every second.                            │  ← whitespace-preserved newline
│                                          │
│ Tap any currency to see the live ...    │
│                                          │
│ • —— • •                  [ Next  → ]    │  ← active dot stretched
└──────────────────────────────────────────┘
```

### Copy

| Step | Tag                       | Title (with `\n`)                                       | Body                                                                          |
| ---- | ------------------------- | ------------------------------------------------------- | ----------------------------------------------------------------------------- |
| 1    | `STEP 01 · LIVE RATES`    | `Every rate.\nEvery second.`                            | Tap any currency to see the live mid-market rate, refreshed from 14 exchanges every second. |
| 2    | `STEP 02 · FEES THAT MATTER` | `See what your\nbank really charges.`                | Compare Wise, Revolut, Western Union and 30+ banks side-by-side — fees, FX margin, total cost. |
| 3    | `STEP 03 · TRAVEL READY`  | `Your wallet\nfollows the map.`                         | Auto-detect local currency on landing. Offline-safe last rates. Per-country tipping built in. |

### Hero glyph

200px circle with `1px accentLine` border. Inside, a 156px dashed circle (`1px dashed accentLine` at 22px inset). Centered: mono glyph at 86px in `accent` color, no weight.

The glyph circle sits over a radial-masked GridBg.

### Dots

Width animates: active = 22px, inactive = 6px. Color: `accent` active, `textGhost` inactive. Height 6px, radius 3px. Transition `width .2s`.

### CTA

Primary button: "Next" for steps 1–2, "Get started" for step 3. With trailing arrow icon.

---

## 2 · Dashboard   (`screens-core.jsx` → `DashboardScreen`)

The default tab. Hero rate (user's pinned pair) + 2×2 mini-bento + favorites list + crypto list.

### Layout

```
[STATUS BAR]

• LIVE  14:32:08 UTC · Wed
Rates
base · USD  ·  12 favorites                  [search icon button]

┌────────────────────────────────────────┐
│  🇪🇺 USD → EUR                  [pinned] │
│                                         │  ← HERO bento
│  0.9182          −0.34%                 │
│                                         │
│  24H RANGE          [sparkline 120×42]  │
│  0.9156 — 0.9241                        │
└────────────────────────────────────────┘

┌─────────────┐ ┌─────────────┐
│ VOLATILITY  │ │ GBP · 1H    │
│ 0.42%       │ │ 0.7841 +.12%│
└─────────────┘ └─────────────┘
┌─────────────┐ ┌─────────────┐
│ JPY · 1H    │ │ ARS · 1H    │
│ 156.42 +.68%│ │ 1182.50 -1.84│
└─────────────┘ └─────────────┘

FAVORITES · 8                          Edit

[CurrencyRow EUR]
[CurrencyRow GBP]
[CurrencyRow JPY]
[CurrencyRow CHF]
[CurrencyRow MXN]

CRYPTO                              See all

[CurrencyRow BTC]
[CurrencyRow ETH]
[CurrencyRow SOL]

[ TAB BAR ]
```

### Hero card

`BentoCard` with internal `GridBg` (masked to fade out). Content stacked:
- Top row: small FlagDot (28dp) + "USD → EUR" + right-aligned `pinned` ghost pill.
- Big number row: mono 48px / 500 / -0.035em + change% mono 14px in up/down color.
- Bottom row: 24h range on left (eyebrow + mono range with em-dash), sparkline 120×42 on right with `dot` enabled, color = `accent`.

### Crypto rows

Same `CurrencyRow` component, just with `kind=Crypto` so the FlagDot renders with violet glyph instead of emoji flag.

---

## 3 · Converter   (`screens-core.jsx` → `ConverterScreen`)

Multi-currency simultaneous (think Convertbot / XCalc): you type in any row, all others update.

### Layout

```
[STATUS BAR]

• MID  14:32 · mid-market

Convert
Multi-currency · live to 4 decimals

┌────────────────────────────────────────┐
│ ╔════════════════════════════════════╗ │  ← focused row gets
│ ║ 🇺🇸 USD                  1,000.00 ║ │     accent-soft bg
│ ║    US Dollar                      ║ │     + 1px accent-line border
│ ╚════════════════════════════════════╝ │
│  🇪🇺 EUR                      918.20    │
│     Euro                                │
│  🇬🇧 GBP                      784.10    │
│     British Pound                       │
│  🇯🇵 JPY                  156,420.00    │
│     Japanese Yen                        │
│  ₿  BTC                     0.01540     │
│     Bitcoin                             │
└────────────────────────────────────────┘

[⇄ Reverse]      [≡ Edit list]

FEES · USD → EUR · €918.20

  Mid-market          [BEST]   €918.20    —
  Wise                          €914.66    $3.85
  Revolut                       €913.41    $5.20
  Chase Bank        [HIGH FEE] €889.10   $31.66

[ TAB BAR ]
```

### Focused row

The active input row has `accentSoft` background, 1px `accentLine` border, and the number is in `accent` color and **size 24** (others are size 19). All other rows are plain. The focused row's number reads as the "amount you're entering", others as derived.

State: a single `focusedCode: String` + `amount: BigDecimal`. Other rows derive `amount * rate[other] / rate[focused]`.

### Fee comparison

Powered by `BankFee` records. The "best" is always mid-market with `—` fee. Banks are flagged HIGH FEE if total cost > 2% of the converted amount.

---

## 4 · Detail   (`screens-detail.jsx` → `DetailScreen`)

Per-currency detail. The chart screen.

### Layout

```
[STATUS BAR]

←                                     ★ Watching   [🔒]
                                                   (bell)
🇪🇺  USD / EUR    Euro

0.9182    ▼ −0.34%

mid-market · 14:32:08 UTC · refresh 1s

┌──────── BigChart ────────────────────┐
│                                        │
│  ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌  0.9241   │
│              ╭─[0.9182 · Apr 28]─╮     │
│  ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌●╌╌╌╌╌╌╌╌╌╌╌╌╌  0.9203 │
│         ╱╲╱   │                        │
│  ╌╌╌╌╌╌╌╌╌╌╌╌╌│╌╌╌╌╌╌╌╌╌╌╌╌╌╌  0.9165 │
│  ──╱        ╲╱                         │
│  ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌  0.9128   │
│                                        │
│  [ 1D | 1W | 1M* | 1Y | ALL ]          │
└────────────────────────────────────────┘

STATISTICS · 1M
  Open       0.9038       Apr 13
  High       0.9241       Apr 28
  Low        0.9028       Apr 02
  Volatility 0.42 %
  Avg 30-day 0.9156

EVENTS · ANNOTATED                    Filter

May 09 · ECB  · Rates held at 3.50% — markets unmoved
Apr 24 · CPI  · Eurozone inflation eases to 2.4% YoY
Apr 11 · FED  · Dovish minutes lift EUR by 0.6%

[ 🔔 Alert me at 0.91 or 0.93              2 active  ]

[ TAB BAR ]
```

### Period switcher

5 tabs in a segmented control. Background `surface2`, padding 3px. Active item: `bg` background, `accent` text, 1px `accentLine` border, 600 weight. Inactive: transparent, `textDim`, 400.

When switching periods, the chart series rebuilds (fetch from `/rates/history/{code}/{period}`) and the focusIndex auto-snaps to ~66% of the new series. No animation — snap.

### Alerts row

A full-width `btn` (variant: ghost). On tap → opens an AlertSetupSheet (bottom sheet) showing all active alerts + "Add new" button.

---

## 5 · Compare   (`screens-detail.jsx` → `CompareScreen`)

8 currencies in a 2-column grid, plus an overlay chart at the bottom.

### Grid card  (`CompareCard`)

```
┌──────────────────────┐
│ 🇪🇺 EUR        -0.34% │
│ 0.9182                │
│ per 1 USD             │
│ ___╲╱╲ . sparkline    │
└──────────────────────┘
```

### Overlay chart

4 normalized lines overlaid: EUR (amber), GBP (sage), JPY (coral), BTC (violet). 130px tall, grid lines at 0/25/50/75/100%. Legend with colored squares below.

**Normalization**: each series is independently scaled to [0,1]. The chart compares *shape*, not absolute price.

---

## 6 · Traveler   (`screens-detail.jsx` → `TravelerScreen`)

Auto-detected location → local currency. Tokyo / JPY is the example state.

### Hero card

GridBg-masked card with:
- Top row: USD flag + "1 USD" + arrow + "JPY" + JPY flag (visual conversion).
- Big mono number `¥156.42` at 56px.
- Caption: "+0.68% today · mid-market".

### Cheat sheet

Static rows: 1, 5, 10, 20, 50, 100 USD → JPY equivalent. Two-column with dashed connector in the middle. Mono throughout.

### Local etiquette grid

3 tiles (one spans 2 cols):
- Tipping · Restaurant — 0% (with "not customary · can offend" caption)
- Tax · Included — 10% (with "consumption tax" caption)
- Cards accepted — Visa / Mastercard / Suica pills + "cash preferred" caption on the right.

These cards are country-specific. Data file: a static table indexed by country code, hydrated from a `/etiquette/{cc}` Firestore doc updated quarterly.

---

## 7 · News   (`screens-extras.jsx` → `NewsScreen`)

Headlines stream + sentiment bar.

### Sentiment bar

A 10px horizontal stacked bar:
- 46% sage (BULLISH)
- 20% textGhost (NEUTRAL)
- 34% coral (BEARISH)

Below: 3-column legend with mono labels.

### Story card

```
[ECB] • HIGH IMPACT                        37m ago

ECB holds key rate at 3.50%, signals patience

Lagarde says inflation path "consistent" but cites
services pressure. EUR ticks down 0.34%.

MOVES   [EUR -0.34%] [CHF -0.08%]
```

Tag chip is uppercase mono on `surface2`, colored by impact level (red/amber/dim). Moves are `Pill` variants.

---

## 8 · Paywall   (`screens-extras.jsx` → `PaywallScreen`)

**iOS-only**. Triggered on:
- 4th open of the app, with a soft dismissable variant.
- Tap on any Pro-gated feature (unlimited alerts, deep history, watch widget, fee comparator).

### Layout

```
                              [×]
FX/ PRO

The full picture.
Every rate. Every market.

Unlimited alerts, deep history, fee comparison,
Watch + widget. All on one membership.

┌─── BENEFIT ROWS ─────────────────────┐
│ ⌖  Live to the second                 │
│    Aggregated mid-market from 14...   │
│ ⬡  Unlimited alerts                   │
│    Price, range, daily and weekly...  │
│ ◐  Traveler mode                      │
│    Auto-location, cheat sheets,...    │
│ ⌘  Real fee comparator                │
│    Wise · Revolut · banks · in...     │
│ ⌬  Apple Watch + widget               │
│    Your favorite pair always one...   │
│ ∞  Unlimited history                  │
│    Down to the minute, back to 2008   │
└───────────────────────────────────────┘

┌── PRICE CARD (1.5px accent border) ──┐
│                          RECOMMENDED │
│ Monthly                              │
│ $2.99 / month                        │
│ Cancel anytime. Billed through       │
│ your Apple ID.                       │
└───────────────────────────────────────┘

[       Start FX/ Pro       ]   ← amber primary

Restore purchase  ·  Terms  ·  Privacy
```

### Behaviour

- Pricing string comes from StoreKit (`SKProduct.localizedPrice`) — never hardcode.
- `Start FX/ Pro` → triggers `Purchase` flow.
- `Restore purchase` → triggers `Restore`.
- Close (×) → soft dismiss for first 3 dismissals. After that, the entry points to Pro features go hard (require purchase).

---

## 9 · Offline   (`screens-core.jsx` → `OfflineScreen`)

Shown when the app has no network and Firestore offline cache has stale data.

### Layout

```
[STATUS BAR]

• OFFLINE                cached · 14:32 UTC

No connection
Showing rates from your last sync · 4 min ago

┌────────────────────────────────────────┐
│  LAST KNOWN · USD → EUR                 │  ← coral eyebrow
│                                         │
│  0.9182                                 │  ← textDim (not full text)
│  14:28:11 UTC  ·  4 min stale           │
└────────────────────────────────────────┘

[ ↻  Retry connection ]   ← amber primary

CACHED FAVORITES

[CurrencyRow EUR @ 0.78 opacity]
[CurrencyRow GBP @ 0.78 opacity]
[CurrencyRow JPY @ 0.78 opacity]
[CurrencyRow CHF @ 0.78 opacity]

╌╌╌  saved locally  ╌╌╌

[ TAB BAR ]
```

The whole favorites list renders at **0.78 opacity** to communicate staleness. The hero number is shown in `textDim`, not full `text`, for the same reason. The eyebrow & accent colors flip to coral throughout this state.
