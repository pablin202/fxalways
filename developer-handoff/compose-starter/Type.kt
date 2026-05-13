package com.yourorg.fx.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

/**
 * FX/ typography · Geist + Geist Mono.
 *
 * Mono is used for ALL numbers (rates, percentages, dates, timestamps,
 * currency codes) and ALL eyebrow / label / tag text. Mono runs with
 * tabular-num + slashed-zero font features so columns of numbers align.
 *
 * Add the font files to `commonMain/composeResources/font/`:
 *   geist_regular.ttf, geist_medium.ttf, geist_semibold.ttf, geist_bold.ttf
 *   geist_mono_regular.ttf, geist_mono_medium.ttf, geist_mono_semibold.ttf
 *
 * Or use `androidx.compose.ui.text.googlefonts.GoogleFont` on Android.
 */
val GeistFontFamily: FontFamily = FontFamily(
    // Font(Res.font.geist_regular,  FontWeight.Normal),
    // Font(Res.font.geist_medium,   FontWeight.Medium),
    // Font(Res.font.geist_semibold, FontWeight.SemiBold),
    // Font(Res.font.geist_bold,     FontWeight.Bold),
)

val GeistMonoFontFamily: FontFamily = FontFamily(
    // Font(Res.font.geist_mono_regular,  FontWeight.Normal),
    // Font(Res.font.geist_mono_medium,   FontWeight.Medium),
    // Font(Res.font.geist_mono_semibold, FontWeight.SemiBold),
)

@Immutable
data class FxTypography(
    // Display & titles (sans)
    val display:    TextStyle,
    val titleXL:    TextStyle,
    val titleL:     TextStyle,

    // Numbers (mono)
    val numberXL:   TextStyle,
    val numberL:    TextStyle,
    val numberBody: TextStyle,

    // Prose (sans)
    val body:       TextStyle,
    val bodyStrong: TextStyle,
    val caption:    TextStyle,

    // Labels & chrome (mono unless noted)
    val captionMono:TextStyle,
    val eyebrow:    TextStyle,
    val pill:       TextStyle,
    val tab:        TextStyle,
)

val defaultFxTypography = FxTypography(
    display = TextStyle(
        fontFamily = GeistFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 36.sp, letterSpacing = (-0.03).em,
    ),
    titleXL = TextStyle(
        fontFamily = GeistFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp, lineHeight = 34.sp, letterSpacing = (-0.03).em,
    ),
    titleL = TextStyle(
        fontFamily = GeistFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 30.sp, letterSpacing = (-0.025).em,
    ),

    numberXL = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 48.sp, lineHeight = 48.sp, letterSpacing = (-0.035).em,
    ),
    numberL = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 19.sp, lineHeight = 22.sp, letterSpacing = (-0.02).em,
    ),
    numberBody = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = (-0.01).em,
    ),

    body = TextStyle(
        fontFamily = GeistFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodyStrong = TextStyle(
        fontFamily = GeistFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = (-0.01).em,
    ),
    caption = TextStyle(
        fontFamily = GeistFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp,
    ),

    captionMono = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.02.em,
    ),
    eyebrow = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 10.5f.sp, lineHeight = 11.sp, letterSpacing = 0.14.em,
    ),
    pill = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 10.5f.sp, lineHeight = 11.sp, letterSpacing = 0.02.em,
    ),
    tab = TextStyle(
        fontFamily = GeistMonoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 10.sp, lineHeight = 11.sp, letterSpacing = 0.06.em,
    ),
)
