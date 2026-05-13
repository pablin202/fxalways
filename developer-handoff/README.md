# FX/ — Currency Exchange · Developer Handoff

Hi-fi design package for a **Kotlin Multiplatform + Compose Multiplatform** currency exchange app, targeting **iOS** (paid · $2.99/mo) and **Android** (free).

This folder is the source of truth for a coding agent (Codex / Claude Code / Cursor) to recreate every screen 1:1 in Compose. Read the docs **in this order**:

| File                    | What's inside                                                          |
| ----------------------- | ---------------------------------------------------------------------- |
| `README.md`             | You are here — overview, project structure, build order                |
| `DESIGN_TOKENS.md`      | Exact colors, typography, spacing, radii, shadows — copy verbatim      |
| `COMPONENTS.md`         | Inventory of every reusable composable (props, behaviour, examples)    |
| `SCREENS.md`            | Per-screen breakdown — layout tree, copy, states                        |
| `ARCHITECTURE.md`       | KMP module structure, Firebase backend, paid-tier gating               |
| `compose-starter/*.kt`  | Pre-written Theme.kt / Color.kt / Type.kt — drop into `commonMain`     |

> The **live HTML mockup** in this project (`index.html` at the repo root) is also part of the handoff — when in doubt, the HTML is canonical. JSX source files (`components.jsx`, `screens-*.jsx`) document exact pixel values, font sizes, and behaviour and should be read alongside this doc.

## Design system in one paragraph

**Bento-mono · warm charcoal · single amber accent.** Big monospace numbers (Geist Mono), Geist sans for prose, generous whitespace, 14–18px corner radii on cards. Everything sits on warm near-black `#0E0E0C` (dark) or warm cream `#F4F0E8` (light). One accent — **amber `#F5A623`** — used for live ticks, CTAs, focus state. Sage green for "up", coral for "down". No gradients. No emoji decoration. Iconography is **1.6px stroke geometric SVG**.

## Build order

1. **Foundation** (week 1):
   1. Set up KMP project (`composeApp` Android, `iosApp` iOS, `shared` common)
   2. Copy `compose-starter/Theme.kt`, `Color.kt`, `Type.kt` into `shared/commonMain/kotlin/.../ui/theme/`
   3. Wire up Geist + Geist Mono fonts as `androidx.compose.ui.text.font.Font` resources
   4. Build the **5 core composables**: `BentoCard`, `FlagDot`, `SparkLine`, `CurrencyRow`, `LiveDot`. See `COMPONENTS.md`
2. **Backend skeleton** (parallel to week 1):
   1. Firebase project + Firestore + Cloud Functions + Scheduler. See `ARCHITECTURE.md`
   2. Scheduler hits Open Exchange Rates + CoinGecko every 30s, writes a `/rates/latest` doc + appends to `/rates/history/{date}`
   3. App reads from Firestore via `firebase-firestore-kmp`. Offline cache is free with Firestore
3. **Screens** (weeks 2–4): build in this order — Dashboard → Detail → Converter → Compare → Traveler → News → Onboarding → Offline → Paywall. See `SCREENS.md`
4. **Paid tier** (week 5): App Store subscription + Firebase user record + Pro-only feature gates (alerts, deep history, watch widget)
5. **Polish** (week 6): Apple Watch companion, iOS widget, Android Material You theming

## KMP project layout

```
fx/
├── shared/
│   └── src/
│       └── commonMain/
│           └── kotlin/com/yourorg/fx/
│               ├── ui/
│               │   ├── theme/      ← Theme.kt, Color.kt, Type.kt (from compose-starter)
│               │   ├── components/ ← BentoCard, FlagDot, SparkLine, CurrencyRow, etc.
│               │   └── screens/    ← DashboardScreen, ConverterScreen, etc.
│               ├── data/
│               │   ├── model/      ← Rate, Currency, Candle, Story
│               │   ├── repo/       ← RatesRepository (Firestore)
│               │   └── cache/      ← Offline cache (free w/ Firestore SDK)
│               └── feature/
│                   ├── alerts/
│                   ├── traveler/   ← location → currency mapping
│                   └── paywall/    ← StoreKit / Play Billing wrapper
├── composeApp/                     ← Android entry point
├── iosApp/                         ← iOS entry point + StoreKit
└── functions/                      ← Firebase Cloud Functions (Node/TS)
    ├── fetchRates.ts               ← scheduled every 30s
    └── stripeWebhook.ts            ← optional, if dual-billing later
```

## What's intentionally NOT specified

- **Navigation library**: pick Voyager or Decompose — both work. The mockup is screen-flat.
- **State management**: ViewModel + StateFlow per screen is fine. No need for fancy reducers.
- **Chart library**: built-in. The `SparkLine` and `BigChart` are pure-Canvas composables; see `COMPONENTS.md` for the algorithm.
- **Icons**: prefer rolling them as `Icons.Outlined.*` from Material Icons Extended — every icon in the mockup is a 16-20px geometric stroke and has a near-equivalent in MIE. Switch to custom SVG only if the design diverges.

## A note on platform divergence

The design **intentionally looks identical on iOS and Android**. Same bento aesthetic, same components, same colors. The only platform-specific surfaces are:
- **System chrome**: iOS uses the OS status bar & home indicator; Android uses Material 3's `WindowInsets`. Both consume `safeContentPadding()`.
- **Tab bar**: Both use a custom bottom bar (NOT `NavigationBar` from M3 — too heavy). Same 5 tabs, same icons, same heights.
- **Keyboards**: native — no work.
- **Paywall**: iOS-only. Android stays free.

See `ARCHITECTURE.md` for the paywall plumbing.
