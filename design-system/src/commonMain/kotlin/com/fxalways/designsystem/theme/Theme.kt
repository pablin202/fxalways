package com.fxalways.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class FxShapes(
    val card: RoundedCornerShape = RoundedCornerShape(18.dp),
    val tile: RoundedCornerShape = RoundedCornerShape(14.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(50),
    val icon: RoundedCornerShape = RoundedCornerShape(10.dp),
    val chip: RoundedCornerShape = RoundedCornerShape(4.dp),
    val field: RoundedCornerShape = RoundedCornerShape(12.dp),
)

val LocalFxColors = staticCompositionLocalOf<FxColors> { error("FxColors not provided") }
val LocalFxTypography = staticCompositionLocalOf { defaultFxTypography }
val LocalFxShapes = staticCompositionLocalOf { FxShapes() }

@Composable
fun FxTheme(
    dark: Boolean = isSystemInDarkTheme(),
    accent: Color = AccentSwatches.amber,
    content: @Composable () -> Unit,
) {
    val colors = if (dark) fxDarkColors(accent) else fxLightColors(accent)
    val typography = defaultFxTypography(
        sans = geistFontFamily(),
        mono = geistMonoFontFamily(),
    )
    CompositionLocalProvider(
        LocalFxColors provides colors,
        LocalFxTypography provides typography,
        LocalFxShapes provides FxShapes(),
    ) {
        MaterialTheme(
            colorScheme = if (dark) {
                darkColorScheme(
                    primary = colors.accent,
                    background = colors.bg,
                    surface = colors.surface1,
                    onSurface = colors.text,
                )
            } else {
                lightColorScheme(
                    primary = colors.accent,
                    background = colors.bg,
                    surface = colors.surface1,
                    onSurface = colors.text,
                )
            },
            typography = typography.asMaterialTypography(),
            content = content,
        )
    }
}

object FxTheme {
    val colors: FxColors @Composable get() = LocalFxColors.current
    val typography: FxTypography @Composable get() = LocalFxTypography.current
    val shapes: FxShapes @Composable get() = LocalFxShapes.current
}
