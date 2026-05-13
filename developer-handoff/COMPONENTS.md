# Components

Every reusable composable, in build order. JSX reference is in parentheses — the JSX is **canonical** for exact pixel values and behavior.

---

## Foundation primitives

### `LiveDot`  (`.live-dot` in `styles.css`)
6×6 amber circle with a 3px wide `accentSoft` halo, pulsing opacity 1.0 → 0.35 → 1.0 at 1.6s ease-in-out infinite. Used in eyebrow rows.

```kotlin
@Composable fun LiveDot(modifier: Modifier = Modifier) { /* infinite transition on alpha */ }
```

### `Eyebrow`  (`.eyebrow` in `styles.css`)
Monospaced uppercase label, 10.5px, letter-spacing 0.14em, color `textFaint` (or `accent` when next to a LiveDot). Stateless text component.

```kotlin
@Composable fun Eyebrow(text: String, color: Color = FxTheme.colors.textFaint)
```

### `Pill`  (`.pill` variants)
Rounded pill with mono text. Variants: `accent` (amber on amberSoft), `up` (sage on sageSoft), `down` (coral on coralSoft), `ghost` (textDim on surface2). Padding `4px 9px`, radius `9999`.

```kotlin
enum class PillVariant { Accent, Up, Down, Ghost }
@Composable fun Pill(text: String, variant: PillVariant = PillVariant.Ghost, leadingIcon: (@Composable ()->Unit)? = null)
```

### `FlagDot`  (`components.jsx` → `FlagDot`)
A round chip holding a flag emoji (fiat) or a mono crypto glyph (₿, Ξ, ◎). Size 24–36px. Background `surface2` for fiat, `surface3` + crypto-purple text for crypto. 1px border `border`.

```kotlin
@Composable fun FlagDot(glyph: String, kind: CurrencyKind = Fiat, size: Dp = 32.dp)
```

Note: on Android, system emoji rendering varies. Consider bundling a flag font (e.g. **Twemoji**) or using a vector flag set for parity. Crypto glyphs render fine from the system mono font.

---

## SparkLine — the hero primitive

```kotlin
@Composable
fun SparkLine(
  values: List<Float>,
  modifier: Modifier = Modifier.size(80.dp, 28.dp),
  color: Color? = null,                     // null = auto (up→sage, down→coral)
  showArea: Boolean = true,                 // gradient fill under line
  showLastDot: Boolean = false,
)
```

**Algorithm** (Canvas-based, no chart library):
1. Find `min` / `max` of values. `range = max - min`. Internal padding **2px**.
2. Map each value to a point: `x = pad + (i / (n-1)) * (w - 2*pad)`, `y = pad + (1 - (v - min) / range) * (h - 2*pad)`.
3. Stroke a smooth path through points (`Path()` with `lineTo` is fine — no need for cubic smoothing; the data is dense enough).
4. Color: `if (values.last() >= values.first()) up else down`, unless `color` is overridden.
5. If `showArea`, fill a vertical gradient from `color @ 30% alpha` (top) to `color @ 0%` (bottom) under the path.
6. If `showLastDot`, draw a 2.2px filled circle at the last point.

**Stroke width**: 1.4px. **Stroke caps**: round. **Stroke joins**: round.

---

## BigChart — for the Detail screen

```kotlin
@Composable
fun BigChart(
  data: List<Float>,
  period: Period,                           // 1D | 1W | 1M | 1Y | ALL
  focusIndex: Int? = null,                  // -1 = right edge ; null = auto (~66% in)
  modifier: Modifier = Modifier.height(200.dp).fillMaxWidth(),
)
```

**Layout**:
- 4 horizontal grid lines (dashed, `border`, 2 4 dasharray). Show price labels on right edge of each, mono 9px `textFaint`.
- Padding `padX=8 padTop=16 padBot=18`.
- Stroke path 1.6px, area gradient `color @ 22% → 0%` alpha.
- **Crosshair**: vertical dashed line at `focusIndex`, `accent`, 0.9px, dasharray `3 3`.
- **Focus circle**: filled 4.5px `accent` + halo 9px `accent @ 18%` alpha.
- **Tooltip pill**: 14×72px amber rect with mono 9.5px text `1A1408` showing `{value} · {date}`. Anchored above the focus point, clamped to chart bounds.

`focusIndex` is set on touch drag; otherwise pre-position around 66% of the series so the chart has visual rhythm even in static state.

---

## Card primitives

### `BentoCard`  (`.bento`)
Standard card. `surface1` background, 1px `border` border, **18dp** radius. Internal padding configurable (default 18dp).

### `BentoTile`  (`.bento-tile`)
Smaller card for 2-column grids. `surface1` background, 1px `border`, **14dp** radius. Internal padding 14dp.

### `GridBg`  (`components.jsx` → `GridBg`)
A faint 24×24 grid pattern (1px lines in `border`) used as decorative texture behind hero numbers. **Always paired with a radial mask** so it fades out before reaching content. Implement with `drawWithCache` + `drawBehind`.

---

## Composite list/row components

### `CurrencyRow`  (`components.jsx` → `CurrencyRow`)
The fundamental list row, used in Dashboard / Compare / Offline.

Layout (h-flex, gap 12, padding 14×16):
```
[FlagDot 34dp]  [Code 15/600 + Name 12/dim]   [SparkLine 56×24]   [Number 14/500 right-aligned]
                [mono caption: "1 USD = 0.9182 EUR"]                [Change% 11 mono up/down]
```

Tap target: full row → opens DetailScreen for that currency.

### `KeyVal`  (`components.jsx` → `KeyVal`)
A 2-column row inside statistics cards.

```
[label 13/textDim] ··········· [mono value 13.5/text]
                                [mono subtitle 10.5/textFaint]
```

Dashed separator (`border` dashed) takes up the middle space.

### `FeeRow`  (`screens-core.jsx` → `FeeRow`)
Used in the Converter fee-comparison block.

```
[provider name]  [BEST | HIGH FEE pill]    [amount mono 13]    [fee mono 11.5/right]
```

Layout: h-flex, padding 12 0. Hairline between rows.

### `EventRow`  (`screens-detail.jsx` → `EventRow`)
Used in Detail screen "Events · annotated".

```
[date mono 11.5 width 38]  [tag pill 9.5]                [→]
                           [headline 13/text, 2-line]
```

---

## Chrome / scaffolding

### `ScreenHeader`
Large title block at top of any screen.
- Optional `sub` (eyebrow) above.
- 28–30sp / 600 / -0.025em title.
- Optional `subtitle` (13/textDim) below.
- Optional `right` slot for an action (e.g. search icon button).

### `SectionTitle`  (`components.jsx` → `SectionTitle`)
A section label inside a screen.
- 12px 18px padding.
- Eyebrow on the left.
- Optional right text in `accent` (e.g. "Edit", "See all").

### `TabBar`  (`components.jsx` → `TabBar`)
Bottom bar with 5 tabs: **Rates · Convert · Compare · Traveler · News**.

- Absolutely positioned at bottom, full width.
- Top border `1px border`.
- Each tab: stacked icon + mono uppercase 10px label, 4px gap.
- Active color = `accent`. Inactive = `textFaint`.
- Tap target: 1/5 of width × 64px height min.
- Respects `safeContentPadding` / `WindowInsets.systemBars`.

### `BigValue`  (`components.jsx` → `BigValue`)
Big monospace number with currency code suffix.

```
[48px mono 500]   [size*0.36 mono dim CODE]
```

Used in: Dashboard hero, Detail header, Converter focused row, Paywall price.

---

## Domain components

### `MiniMetric`  (in `screens-core.jsx`)
Small bento tile with eyebrow + value + sub. Used in Dashboard's 2×2 metric grid.

### `CompareCard`  (in `screens-detail.jsx`)
A 2-column card in CompareScreen.
- FlagDot 24 + code + change% pill.
- Big mono number.
- "per 1 USD" mono caption.
- Full-width sparkline at bottom.

### `OverlayChart`  (in `screens-detail.jsx`)
Multi-series normalized line chart. Each series normalized to [0,1] independently so they all stretch the chart height regardless of scale (i.e. shapes are compared, not absolute values).

### `MiniMetric`, `LegendDot`, `MoveBadge`
Small leaf components — see JSX for full specs.

---

## Component naming for Compose

| JSX name           | Compose name           |
| ------------------ | ---------------------- |
| `CurrencyRow`      | `CurrencyRow`          |
| `FlagDot`          | `FlagDot`              |
| `SparkLine`        | `SparkLine`            |
| `BigChart`         | `PriceChart`           |
| `Pill`             | `Pill`                 |
| `MiniMetric`       | `MetricTile`           |
| `CompareCard`      | `CompareTile`          |
| `KeyVal`           | `KeyValueRow`          |
| `FeeRow`           | `FeeComparisonRow`     |
| `EventRow`         | `EventRow`             |
| `BentoCard`        | `BentoCard`            |
| `BentoTile`        | `BentoTile`            |
| `ScreenHeader`     | `ScreenHeader`         |
| `SectionTitle`     | `SectionLabel`         |
| `TabBar`           | `FxBottomBar`          |
| `LiveDot`          | `LiveDot`              |
| `Eyebrow`          | `Eyebrow`              |
| `BigValue`         | `BigValueText`         |

(Compose conventions: PascalCase, suffix nouns where ambiguous.)
