package com.yourorg.fx.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * FX/ color palette · bento-mono · warm charcoal / cream.
 *
 * All accents share the same chroma & lightness — only hue varies — so
 * weight stays consistent across user-selectable accents.
 */
@Immutable
data class FxColors(
    // surfaces
    val bg:          Color,
    val bg2:         Color,
    val surface1:    Color,
    val surface2:    Color,
    val surface3:    Color,
    val border:      Color,
    val border2:     Color,

    // text
    val text:        Color,
    val textDim:     Color,
    val textFaint:   Color,
    val textGhost:   Color,

    // accent (user-tweakable, default amber)
    val accent:      Color,
    val accentSoft:  Color,
    val accentLine:  Color,

    // semantic
    val up:          Color,
    val upSoft:      Color,
    val down:        Color,
    val downSoft:    Color,
    val crypto:      Color,

    val isLight:     Boolean,
)

object AccentSwatches {
    val amber  = Color(0xFFF5A623)
    val violet = Color(0xFFB79CF7)
    val mint   = Color(0xFF6FD4B0)
    val coral  = Color(0xFFF08A6A)
}

object SemanticSwatches {
    val up     = Color(0xFF94D082)   // sage — gains, BUY, positive change
    val down   = Color(0xFFE07856)   // coral — losses, SELL, negative change
    val crypto = Color(0xFFC7A6F5)   // soft violet — crypto badges
}

fun fxDarkColors(accent: Color = AccentSwatches.amber): FxColors = FxColors(
    bg          = Color(0xFF0E0E0C),
    bg2         = Color(0xFF131311),
    surface1    = Color(0xFF1C1C18),
    surface2    = Color(0xFF232320),
    surface3    = Color(0xFF2A2A26),
    border      = Color(0xFFF4F0E8).copy(alpha = 0.08f),
    border2     = Color(0xFFF4F0E8).copy(alpha = 0.14f),

    text        = Color(0xFFF4F0E8),
    textDim     = Color(0xFFF4F0E8).copy(alpha = 0.62f),
    textFaint   = Color(0xFFF4F0E8).copy(alpha = 0.38f),
    textGhost   = Color(0xFFF4F0E8).copy(alpha = 0.20f),

    accent      = accent,
    accentSoft  = accent.copy(alpha = 0.14f),
    accentLine  = accent.copy(alpha = 0.28f),

    up          = SemanticSwatches.up,
    upSoft      = SemanticSwatches.up.copy(alpha = 0.14f),
    down        = SemanticSwatches.down,
    downSoft    = SemanticSwatches.down.copy(alpha = 0.14f),
    crypto      = SemanticSwatches.crypto,

    isLight     = false,
)

fun fxLightColors(accent: Color = AccentSwatches.amber): FxColors = FxColors(
    bg          = Color(0xFFF4F0E8),
    bg2         = Color(0xFFEBE6DC),
    surface1    = Color(0xFFFFFFFF),
    surface2    = Color(0xFFF8F4EC),
    surface3    = Color(0xFFEFEAE0),
    border      = Color(0xFF18181C).copy(alpha = 0.08f),
    border2     = Color(0xFF18181C).copy(alpha = 0.14f),

    text        = Color(0xFF18181C),
    textDim     = Color(0xFF18181C).copy(alpha = 0.62f),
    textFaint   = Color(0xFF18181C).copy(alpha = 0.40f),
    textGhost   = Color(0xFF18181C).copy(alpha = 0.18f),

    accent      = accent,
    accentSoft  = accent.copy(alpha = 0.16f),
    accentLine  = accent.copy(alpha = 0.34f),

    up          = SemanticSwatches.up,
    upSoft      = SemanticSwatches.up.copy(alpha = 0.16f),
    down        = SemanticSwatches.down,
    downSoft    = SemanticSwatches.down.copy(alpha = 0.14f),
    crypto      = SemanticSwatches.crypto,

    isLight     = true,
)
