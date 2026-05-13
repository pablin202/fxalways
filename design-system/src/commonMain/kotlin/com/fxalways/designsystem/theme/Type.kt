package com.fxalways.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import currencyexchangekmp.design_system.generated.resources.Res
import currencyexchangekmp.design_system.generated.resources.geist_bold
import currencyexchangekmp.design_system.generated.resources.geist_medium
import currencyexchangekmp.design_system.generated.resources.geist_mono_medium
import currencyexchangekmp.design_system.generated.resources.geist_mono_regular
import currencyexchangekmp.design_system.generated.resources.geist_mono_semibold
import currencyexchangekmp.design_system.generated.resources.geist_regular
import currencyexchangekmp.design_system.generated.resources.geist_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun geistFontFamily(): FontFamily = FontFamily(
    Font(Res.font.geist_regular, FontWeight.Normal),
    Font(Res.font.geist_medium, FontWeight.Medium),
    Font(Res.font.geist_semibold, FontWeight.SemiBold),
    Font(Res.font.geist_bold, FontWeight.Bold),
)

@Composable
fun geistMonoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.geist_mono_regular, FontWeight.Normal),
    Font(Res.font.geist_mono_medium, FontWeight.Medium),
    Font(Res.font.geist_mono_semibold, FontWeight.SemiBold),
)

@Immutable
data class FxTypography(
    val display: TextStyle,
    val titleXL: TextStyle,
    val titleL: TextStyle,
    val numberXL: TextStyle,
    val numberL: TextStyle,
    val numberBody: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val caption: TextStyle,
    val captionMono: TextStyle,
    val eyebrow: TextStyle,
    val pill: TextStyle,
    val tab: TextStyle,
)

fun defaultFxTypography(
    sans: FontFamily = FontFamily.Default,
    mono: FontFamily = FontFamily.Monospace,
) = FxTypography(
    display = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 36.sp, letterSpacing = (-0.03).em),
    titleXL = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 34.sp, letterSpacing = (-0.03).em),
    titleL = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 30.sp, letterSpacing = (-0.025).em),
    numberXL = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 48.sp, lineHeight = 48.sp, letterSpacing = (-0.035).em),
    numberL = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 19.sp, lineHeight = 22.sp, letterSpacing = (-0.02).em),
    numberBody = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = (-0.01).em),
    body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodyStrong = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = (-0.01).em),
    caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    captionMono = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.02.em),
    eyebrow = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, lineHeight = 11.sp, letterSpacing = 0.14.em),
    pill = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, lineHeight = 11.sp, letterSpacing = 0.02.em),
    tab = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 11.sp, letterSpacing = 0.06.em),
)

val defaultFxTypography = defaultFxTypography()

fun FxTypography.asMaterialTypography(): Typography = Typography(
    displayLarge = display,
    displayMedium = display,
    displaySmall = titleXL,
    headlineLarge = titleL,
    headlineMedium = titleL,
    headlineSmall = titleL.copy(fontSize = 24.sp, lineHeight = 28.sp),
    titleLarge = bodyStrong.copy(fontSize = 20.sp, lineHeight = 24.sp),
    titleMedium = bodyStrong,
    titleSmall = bodyStrong.copy(fontSize = 13.sp, lineHeight = 17.sp),
    bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = body,
    bodySmall = caption,
    labelLarge = bodyStrong,
    labelMedium = pill,
    labelSmall = tab,
)
