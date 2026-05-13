# FX Always — App icons

Master mark: **"FX/"** in mono semibold — cream "FX" with amber "/" on warm charcoal background. Subtle amber radial wash from upper-left for depth.

## iOS — `icons/ios/`

13 sizes, no rounded corners (the system applies the mask). Drop into your `Assets.xcassets/AppIcon.appiconset/` and reference from `Contents.json`. Or just import the 1024 master and let Xcode generate the rest.

| File              | Use                                      |
| ----------------- | ---------------------------------------- |
| `icon-1024.png`   | App Store marketing                      |
| `icon-180.png`    | iPhone 60pt @3x (home screen)            |
| `icon-167.png`    | iPad Pro 83.5pt @2x                      |
| `icon-152.png`    | iPad 76pt @2x                            |
| `icon-120.png`    | iPhone 60pt @2x · 40pt @3x (Spotlight)   |
| `icon-87.png`     | Settings 29pt @3x                        |
| `icon-80.png`     | Spotlight 40pt @2x                       |
| `icon-76.png`     | iPad 76pt @1x (legacy)                   |
| `icon-60.png`     | Spotlight 40pt @1.5x                     |
| `icon-58.png`     | Settings 29pt @2x                        |
| `icon-40.png`     | Spotlight 40pt @1x                       |
| `icon-29.png`     | Settings 29pt @1x                        |
| `icon-20.png`     | Notifications 20pt @1x                   |

## Android — `icons/android/`

### Adaptive icon (Android 8.0+) — `adaptive/`

Three layers, 432×432 each (xxxhdpi rendering of a 108dp adaptive icon). The mark fits in the central 66% safe area so it survives every launcher mask (circle, squircle, teardrop, square).

| File                            | Use                                          |
| ------------------------------- | -------------------------------------------- |
| `ic_launcher_foreground.png`    | Foreground (transparent bg, glyph only)      |
| `ic_launcher_background.png`    | Background (solid charcoal, no glyph)        |
| `ic_launcher_monochrome.png`    | Themed icon for Android 13+ (Material You)  |

Wire it in `res/mipmap-anydpi-v26/ic_launcher.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_monochrome"/>
</adaptive-icon>
```

### Legacy launcher icons — `mipmap-{bucket}/`

For Android 7 and below. Already include a 22% rounded-square mask baked in (`ic_launcher.png`) and a 50% circle mask (`ic_launcher_round.png`).

| Bucket    | Size    |
| --------- | ------- |
| mdpi      | 48×48   |
| hdpi      | 72×72   |
| xhdpi     | 96×96   |
| xxhdpi    | 144×144 |
| xxxhdpi   | 192×192 |

Drop the `mipmap-*` folders straight into `composeApp/src/androidMain/res/`.

### Play Store listing

`play-store-512.png` — 512×512, the high-res icon required by Play Console.

## Master files — `icons/`

- `master-1024.png` — full 1024 with no clip, the canonical mark
- `master-rounded-1024.png` — same with iOS-style 22% rounded corners baked in

---

## Regenerating

The source generator lives in the chat history. The full design (colors, geometry, safe areas) is in `developer-handoff/DESIGN_TOKENS.md` under "Brand mark". To re-export at a new size, render at native size — don't upscale from a smaller PNG, since the mono glyph hinting differs at each size.
