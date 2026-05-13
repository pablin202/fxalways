package com.yourorg.fx.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * FX/ shapes — bento aesthetic. Sharp-ish, not pill-y.
 */
@Immutable
data class FxShapes(
    val card:  RoundedCornerShape = RoundedCornerShape(18.dp),
    val tile:  RoundedCornerShape = RoundedCornerShape(14.dp),
    val pill:  RoundedCornerShape = RoundedCornerShape(50),
    val icon:  RoundedCornerShape = RoundedCornerShape(10.dp),
    val chip:  RoundedCornerShape = RoundedCornerShape(4.dp),
    val field: RoundedCornerShape = RoundedCornerShape(12.dp),
)

val LocalFxColors      = staticCompositionLocalOf<FxColors>     { error("FxColors not provided")     }
val LocalFxTypography  = staticCompositionLocalOf<FxTypography> { defaultFxTypography                }
val LocalFxShapes      = staticCompositionLocalOf<FxShapes>     { FxShapes()                          }

/**
 * Single entry point. Wrap your app in `FxTheme { … }`.
 *
 * @param dark if true, dark color tokens are used (default)
 * @param accent user-selectable accent (amber default)
 */
@Composable
fun FxTheme(
    dark: Boolean = true,
    accent: androidx.compose.ui.graphics.Color = AccentSwatches.amber,
    content: @Composable () -> Unit,
) {
    val colors = if (dark) fxDarkColors(accent) else fxLightColors(accent)
    val shapes = FxShapes()

    CompositionLocalProvider(
        LocalFxColors      provides colors,
        LocalFxTypography  provides defaultFxTypography,
        LocalFxShapes      provides shapes,
    ) {
        // We piggyback Material 3 only for ripples, gesture insets and
        // a few defaulted text styles. Our own composables read FxTheme,
        // not MaterialTheme.colorScheme.
        MaterialTheme(
            colorScheme = if (dark) {
                darkColorScheme(
                    primary    = colors.accent,
                    background = colors.bg,
                    surface    = colors.surface1,
                    onSurface  = colors.text,
                )
            } else {
                lightColorScheme(
                    primary    = colors.accent,
                    background = colors.bg,
                    surface    = colors.surface1,
                    onSurface  = colors.text,
                )
            },
            content = content,
        )
    }
}

/** Convenience accessors. */
object FxTheme {
    val colors:     FxColors     @Composable get() = LocalFxColors.current
    val typography: FxTypography @Composable get() = LocalFxTypography.current
    val shapes:     FxShapes     @Composable get() = LocalFxShapes.current
}
