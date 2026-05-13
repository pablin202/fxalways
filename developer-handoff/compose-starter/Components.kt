package com.yourorg.fx.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yourorg.fx.ui.theme.FxTheme

/**
 * Reference implementations for the workhorse composables.
 *
 * Not exhaustive — see `developer-handoff/COMPONENTS.md` for the full
 * inventory. These show the patterns: read tokens from FxTheme, use Canvas
 * for SVG-style charts, lean on simple Box/Row/Column layouts.
 */

// ── SparkLine ───────────────────────────────────────────────────────────

@Composable
fun SparkLine(
    values: List<Float>,
    modifier: Modifier = Modifier.size(80.dp, 28.dp),
    color: Color? = null,
    showArea: Boolean = true,
    showLastDot: Boolean = false,
) {
    if (values.size < 2) return
    val colors = FxTheme.colors
    val auto = if (values.last() >= values.first()) colors.up else colors.down
    val stroke = color ?: auto

    Canvas(modifier = modifier) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).coerceAtLeast(1e-9f)
        val pad = 2f
        val w = size.width  - pad * 2
        val h = size.height - pad * 2

        val pts = values.mapIndexed { i, v ->
            Offset(
                x = pad + (i.toFloat() / (values.size - 1)) * w,
                y = pad + (1f - (v - min) / range) * h,
            )
        }

        val line = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }

        if (showArea) {
            val area = Path().apply {
                addPath(line)
                lineTo(pts.last().x, size.height - pad)
                lineTo(pad, size.height - pad)
                close()
            }
            drawPath(
                area,
                Brush.verticalGradient(
                    0.0f to stroke.copy(alpha = 0.30f),
                    1.0f to stroke.copy(alpha = 0.00f),
                ),
            )
        }

        drawPath(
            line,
            color = stroke,
            style = Stroke(width = 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        if (showLastDot) {
            drawCircle(stroke, radius = 2.2f, center = pts.last())
        }
    }
}

// ── FlagDot ─────────────────────────────────────────────────────────────

enum class CurrencyKind { Fiat, Crypto }

@Composable
fun FlagDot(
    glyph: String,
    kind: CurrencyKind = CurrencyKind.Fiat,
    size: Dp = 32.dp,
) {
    val colors = FxTheme.colors
    val bg     = if (kind == CurrencyKind.Crypto) colors.surface3 else colors.surface2
    val tint   = if (kind == CurrencyKind.Crypto) colors.crypto   else colors.text
    val font   = if (kind == CurrencyKind.Crypto)
        FxTheme.typography.numberL.copy(fontSize = (size.value * 0.46f).sp)
    else
        FxTheme.typography.body.copy(fontSize = (size.value * 0.62f).sp)

    Box(
        modifier = Modifier
            .size(size)
            .background(bg, CircleShape)
            .border(1.dp, colors.border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = font, color = tint)
    }
}

// ── CurrencyRow ─────────────────────────────────────────────────────────

data class Rate(
    val code: String,
    val name: String,
    val glyph: String,
    val kind: CurrencyKind,
    val rate: Double,
    val change24h: Double,
    val sparkline: List<Float>,
)

@Composable
fun CurrencyRow(
    rate: Rate,
    onClick: () -> Unit = {},
    dense: Boolean = false,
) {
    val colors = FxTheme.colors
    val type   = FxTheme.typography
    val isUp   = rate.change24h >= 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical   = if (dense) 10.dp else 14.dp,
            ),
    ) {
        FlagDot(rate.glyph, rate.kind, size = if (dense) 28.dp else 34.dp)

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rate.code, style = type.bodyStrong, color = colors.text)
                Text(rate.name, style = type.caption, color = colors.textFaint, maxLines = 1)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "1 USD = ${formatRate(rate.rate)} ${rate.code}",
                style = type.captionMono,
                color = colors.textDim,
            )
        }

        SparkLine(values = rate.sparkline, modifier = Modifier.size(56.dp, 24.dp))

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 56.dp)) {
            Text(formatRate(rate.rate), style = type.numberL, color = colors.text)
            Spacer(Modifier.height(2.dp))
            Text(
                formatChange(rate.change24h),
                style = type.captionMono,
                color = if (isUp) colors.up else colors.down,
            )
        }
    }
}

// number formatting — tuned to mimic cxFmt() in components.jsx
private fun formatRate(n: Double): String {
    val abs = kotlin.math.abs(n)
    val d = when {
        abs >= 1000  -> 2
        abs >= 100   -> 3
        abs >= 1     -> 4
        abs >= 0.01  -> 4
        else         -> 7
    }
    // Group thousands; format with d decimals.
    return "%,.${d}f".format(n)
}

private fun formatChange(c: Double): String =
    if (c >= 0) "+%.2f%%".format(c) else "%.2f%%".format(c)
