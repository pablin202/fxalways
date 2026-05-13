# Prompt for Codex (or any coding agent)

Paste this prompt into Codex (or Claude Code / Cursor) along with attaching the entire `developer-handoff/` folder.

---

## Begin prompt

```
You are building a Kotlin Multiplatform + Compose Multiplatform currency
exchange app called "FX/". You have been given a complete design package in
the `developer-handoff/` folder. Your job is to recreate every screen 1:1
in Compose, matching the visual design exactly.

# Source of truth, in priority order

1.  developer-handoff/SCREENS.md         — per-screen layouts and copy
2.  developer-handoff/COMPONENTS.md      — composable inventory
3.  developer-handoff/DESIGN_TOKENS.md   — exact color/type/spacing values
4.  developer-handoff/ARCHITECTURE.md    — KMP modules + Firebase backend
5.  developer-handoff/compose-starter/   — Theme.kt, Color.kt, Type.kt,
                                            Components.kt (drop in as-is)
6.  Live HTML mockup at the project root  — when in doubt, look at the JSX:
     - components.jsx       (FlagDot, SparkLine, CurrencyRow, …)
     - screens-core.jsx     (Onboarding, Dashboard, Converter, Offline)
     - screens-detail.jsx   (Detail, Compare, Traveler, BigChart)
     - screens-extras.jsx   (News, Paywall)
     - styles.css           (tokens and helper classes)

# Build order (strict)

Week 1 — Foundation:
  1. Init a KMP project with composeApp (Android), iosApp, shared modules.
  2. Add Google Fonts: Geist + Geist Mono. Wire as FontFamily.
  3. Drop in compose-starter/Theme.kt, Color.kt, Type.kt, Components.kt
     under shared/src/commonMain/kotlin/com/yourorg/fx/ui/theme/ and
     /components/.
  4. Verify SparkLine, FlagDot, CurrencyRow render with sample data.

Week 2 — Backend skeleton (parallel):
  1. Create Firebase project. Enable Firestore + Auth + Functions + Scheduler.
  2. Implement functions/fetchLatestRates.ts hitting Open Exchange Rates +
     CoinGecko, writing to /rates/latest every 30s.
  3. Add KMP firebase-firestore dependency. Implement FirebaseRatesRepository
     per the interface in ARCHITECTURE.md.

Week 3 — Screens (in this order — Dashboard is highest signal):
  Dashboard → Detail → Converter → Compare → Traveler → News → Onboarding
  → Offline → Paywall.

  For each screen:
    a. Open the JSX file referenced in SCREENS.md.
    b. Match the layout tree exactly. Read pixel values from the JSX.
    c. Pull copy verbatim from SCREENS.md.
    d. Wire to RatesRepository / Mock data initially.

Week 4 — Paid tier:
  Implement StoreKit subscription flow per ARCHITECTURE.md. iOS only.
  Android stays free. ProGate composable gates Pro features.

# Rules

1. **Match the design pixel-perfectly.** Do not paraphrase the design.
   Every number, spacing, color, and font weight is in the spec. Use them.
2. **Mono for ALL numbers**, no exceptions. Eyebrows are mono uppercase too.
3. **No gradients, no emoji decoration, no shadows on dark.** Bento is flat.
4. **Read FxTheme tokens** — never inline hex values inside composables.
5. **No Material 3 components for primary surfaces.** Build BentoCard /
   BentoTile from scratch. M3 is only for Ripple + InsetsHelpers.
6. **All accents share chroma/lightness.** When user picks "violet", swap
   `accent`. Don't recolor up/down/crypto.
7. **iOS-only paywall.** Build with expect/actual flag. Android binary
   should not include the paywall composable.

# Definition of done (per screen)

- All copy from SCREENS.md is present.
- Layout matches the JSX visually at iPhone 16 dimensions (402×874) and
  Pixel 8 (412×892).
- Dark and light modes both render correctly.
- Data is wired to RatesRepository.latestFlow() (or a Mock for screens
  not yet hooked up).
- Loading and offline states are handled.
- Light tests for any non-trivial layout (e.g. SparkLine with 1 value,
  empty favorites, etc.).

Start by reading README.md, then DESIGN_TOKENS.md, then the JSX files for
the Dashboard. Build CurrencyRow + the Dashboard layout first; that single
screen exercises 70% of the design system.
```

## End prompt

---

## Tips for the human

- **Attach the whole `developer-handoff/` folder** to the chat. Don't paste files inline.
- **Also share the live HTML preview URL** (or the project ZIP). The JSX files are the canonical pixel reference and you want the agent to be able to grep them.
- **Start with one screen end-to-end** before parallelizing. The Dashboard exercises the most components.
- **Don't let the agent invent components.** If it does, point it at COMPONENTS.md and the existing JSX equivalent.
- **Push back on Material 3 defaults.** Codex will reach for `NavigationBar`, `Card`, `OutlinedTextField` etc. — none of those match this design. Force it to use the custom `BentoCard`, `FxBottomBar`, etc.
- **Iterate on the design loop**: build a screen → screenshot the Compose preview → put screenshot + JSX rendering side-by-side → ask Codex to diff and fix.
