# Design Tokens

All token values are **exact** — copy these verbatim. Source is `styles.css` in the project root.

---

## Color (Dark — default)

| Token             | Hex / value                    | Usage                                        |
| ----------------- | ------------------------------ | -------------------------------------------- |
| `bg`              | `#0E0E0C`                      | Page background                              |
| `bg2`             | `#131311`                      | Slight elevation below bg                    |
| `surface1`        | `#1C1C18`                      | Card / bento background                      |
| `surface2`        | `#232320`                      | Inset / nested card                          |
| `surface3`        | `#2A2A26`                      | Highest elevation                            |
| `border`          | `rgba(244,240,232,0.08)`       | Hairline borders                             |
| `border2`         | `rgba(244,240,232,0.14)`       | Emphasized borders                           |
| `text`            | `#F4F0E8`                      | Primary text                                 |
| `textDim`         | `rgba(244,240,232,0.62)`       | Secondary text                               |
| `textFaint`       | `rgba(244,240,232,0.38)`       | Captions, labels                             |
| `textGhost`       | `rgba(244,240,232,0.20)`       | Disabled, separators                         |
| **`accent`**      | **`#F5A623`** (amber)          | **Live ticks, CTAs, focused state, brand**   |
| `accentSoft`      | `rgba(245,166,35,0.14)`        | Accent backgrounds                           |
| `accentLine`      | `rgba(245,166,35,0.28)`        | Accent borders                               |
| `up`              | `#94D082` (sage)               | Positive change, BUY, gains                  |
| `upSoft`          | `rgba(148,208,130,0.14)`       | Positive pill backgrounds                    |
| `down`            | `#E07856` (coral)              | Negative change, SELL, losses                |
| `downSoft`        | `rgba(224,120,86,0.14)`        | Negative pill backgrounds                    |
| `crypto`          | `#C7A6F5` (soft violet)        | Crypto-only badges / glyphs                  |

## Color (Light)

| Token             | Hex / value                    |
| ----------------- | ------------------------------ |
| `bg`              | `#F4F0E8`                      |
| `bg2`             | `#EBE6DC`                      |
| `surface1`        | `#FFFFFF`                      |
| `surface2`        | `#F8F4EC`                      |
| `surface3`        | `#EFEAE0`                      |
| `border`          | `rgba(28,28,24,0.08)`          |
| `text`            | `#18181C`                      |
| `textDim`         | `rgba(24,24,28,0.62)`          |
| `textFaint`       | `rgba(24,24,28,0.40)`          |

> `accent`, `up`, `down`, `crypto` are **the same in light and dark**. Only neutrals flip.

## Tweakable accent palette

User can swap accent in app settings. All accents share the **same chroma & lightness**, only hue varies — so contrast & weight stay constant.

| Name    | Hex       |
| ------- | --------- |
| amber   | `#F5A623` |
| violet  | `#B79CF7` |
| mint    | `#6FD4B0` |
| coral   | `#F08A6A` |

---

## Typography

Two families, both **Google Fonts**:
- **Geist** (sans) — weights 300, 400, 500, 600, 700
- **Geist Mono** — weights 400, 500, 600

Mono is used for **all numbers** (rates, percentages, dates, timestamps, codes) and **all eyebrow / label / tag text** (uppercase, letterspaced). Sans is used for prose, headlines, button labels.

Mono is set with `font-feature-settings: 'zero', 'tnum'` — slashed zero + tabular numbers — so columns of numbers align.

### Type scale

| Role            | Family     | Size  | Weight | Line ht | Letter-spacing | Notes                                  |
| --------------- | ---------- | ----- | ------ | ------- | -------------- | -------------------------------------- |
| Display         | Geist      | 36    | 600    | 1.00    | -0.030em       | Paywall / onboarding hero              |
| Title XL        | Geist      | 32    | 600    | 1.05    | -0.030em       | Onboarding step title                  |
| Title L         | Geist      | 28–30 | 600    | 1.05    | -0.025em       | Screen title (Dashboard "Rates")       |
| Number XL       | Geist Mono | 48–56 | 500    | 1.00    | -0.035em       | Hero rate / paywall price              |
| Number L        | Geist Mono | 19–24 | 500    | 1.10    | -0.020em       | Card primary number                    |
| Body            | Geist      | 14–15 | 400    | 1.45    | 0              | Paragraphs, descriptions               |
| Body Strong     | Geist      | 14–15 | 500–600| 1.30    | -0.010em       | List headlines, button labels          |
| Number Body     | Geist Mono | 13–14 | 400–500| 1.20    | -0.010em       | Stats values                            |
| Caption         | Geist      | 12–13 | 400    | 1.40    | 0              | Secondary copy                         |
| Caption Mono    | Geist Mono | 11–12 | 400    | 1.20    | 0.020em        | Mid-market, "1 USD = 0.9182 EUR"       |
| **Eyebrow**     | Geist Mono | 10.5  | 400    | 1.00    | 0.140em + UPPER| Section labels, tags                    |
| Pill            | Geist Mono | 10.5–11 | 400  | 1.00    | 0.020em        | Pill content, change %                  |
| Tab label       | Geist Mono | 10    | 400    | 1.00    | 0.060em + UPPER| Bottom tab bar                          |

### Compose mapping

```kotlin
@Immutable
data class FxTypography(
  val display:    TextStyle,
  val titleXL:    TextStyle,
  val titleL:     TextStyle,
  val numberXL:   TextStyle,
  val numberL:    TextStyle,
  val body:       TextStyle,
  val bodyStrong: TextStyle,
  val numberBody: TextStyle,
  val caption:    TextStyle,
  val captionMono:TextStyle,
  val eyebrow:    TextStyle,
  val pill:       TextStyle,
  val tab:        TextStyle,
)

val LocalFxTypography = staticCompositionLocalOf { defaultFxTypography }
```

See `compose-starter/Type.kt` for the filled values.

---

## Spacing

The grid is **4px-based**. Common values:

```
4   8   10   12   14   16   18   20   22   26   32   40   48
```

Page horizontal padding: **18px** consistently (`pad-x` helper class in CSS, `Modifier.padding(horizontal = 18.dp)` in Compose).

Card internal padding: **14–18px**.

Vertical rhythm between sections: **12–24px**.

Section title to first row: **6px**.

---

## Radii

| Token       | Value | Where                                          |
| ----------- | ----- | ---------------------------------------------- |
| `rCard`     | 18 dp | Bento cards, hero card                         |
| `rTile`     | 14 dp | Mini-bento tiles, segmented controls           |
| `rPill`     | 9999  | Pills, chips, FlagDot, the live dot itself     |
| `rIcon`     | 10 dp | Icon badges in lists                           |
| `rChip`     | 4 dp  | Tag chips (tag bg behind "ECB", "BTC")         |
| `rField`    | 12 dp | Input fields, segmented control items          |

In Compose:
```kotlin
@Immutable
data class FxShapes(
  val card: RoundedCornerShape  = RoundedCornerShape(18.dp),
  val tile: RoundedCornerShape  = RoundedCornerShape(14.dp),
  val pill: RoundedCornerShape  = RoundedCornerShape(50),
  val icon: RoundedCornerShape  = RoundedCornerShape(10.dp),
  val chip: RoundedCornerShape  = RoundedCornerShape(4.dp),
  val field: RoundedCornerShape = RoundedCornerShape(12.dp),
)
```

---

## Elevation / shadows

Almost **none**. Bento is flat. Only two shadow recipes:

1. **Light-mode card subtle shadow**: `0 1px 0 rgba(28,28,24,0.04)` — barely there, just to detach card from cream bg.
2. **Focus / lifted state**: `0 8px 24px rgba(0,0,0,0.18)` — only used during drag/swap interactions.

Dark mode uses no shadows; cards are separated purely by 1px borders in `border` (8% opacity warm white).

---

## Motion

Used sparingly:

- **Live dot pulse**: `0% → 100%` opacity 1 → 0.35 → 1, 1.6s ease-in-out infinite. See `.live-dot` in `styles.css`.
- **Number shimmer on update**: text color fades through accent on rate change. `cx-shimmer` keyframe.
- **Segmented tab switch**: 200ms ease pill width on dot indicators.
- **Currency row tap → detail**: 220ms ease-out shared element transition on `FlagDot` + code text.

Everything else snaps. **No springs, no parallax, no blur transitions.**

---

## Iconography

All icons are **16–20px geometric strokes**, `stroke-width: 1.6–1.8`, `stroke-linecap: round`, `stroke-linejoin: round`. **No fill icons** except the live dot and the watching star.

For Compose: use `androidx.compose.material.icons.outlined.*` as starting point, override stroke if needed. Custom icons (e.g. the tab bar set) are inline `vectorResource` SVGs.

---

## Live-state visual vocabulary

- **`LIVE` eyebrow + amber pulsing dot**: data is live-streaming.
- **`MID` eyebrow + amber static dot**: showing mid-market (no spread).
- **`OFFLINE` eyebrow + coral static dot**: data is stale, last sync timestamp shown.
- **`CACHED · 14:32 UTC`** in mono caption: explicit stale-data disclosure.

These three states are mutually exclusive in any given view.
