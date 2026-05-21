package com.fxalways.app.screens.detail

import com.fxalways.app.screens.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, FxTheme.shapes.chip))
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
    }
}

@Composable
internal fun SentimentBar(
    bullish: Int = 46,
    neutral: Int = 20,
    bearish: Int = 34,
) {
    Row(Modifier.fillMaxWidth().height(10.dp).clip(FxTheme.shapes.pill)) {
        Box(Modifier.weight(bullish.coerceAtLeast(1).toFloat()).background(FxTheme.colors.up))
        Box(Modifier.weight(neutral.coerceAtLeast(1).toFloat()).background(FxTheme.colors.textGhost))
        Box(Modifier.weight(bearish.coerceAtLeast(1).toFloat()).background(FxTheme.colors.down))
    }
}

@Composable
internal fun OverlayChart(rates: List<FxRate>) {
    val border = FxTheme.colors.border
    val series = rates.map { rate -> rate.sparkline.normalizedPercentSeries() }
    val colors = rates.mapIndexed { index, rate -> compareOverlayColor(index, rate.kind) }
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
        repeat(5) { i ->
            val y = size.height * (i / 4f)
            drawLine(border, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        series.forEachIndexed { seriesIndex, values ->
            val min = values.minOrNull() ?: return@forEachIndexed
            val max = values.maxOrNull() ?: return@forEachIndexed
            val range = (max - min).coerceAtLeast(1e-9f)
            val path = Path()
            values.forEachIndexed { index, value ->
                val denominator = values.lastIndex.coerceAtLeast(1)
                val point = Offset(
                    x = (index.toFloat() / denominator) * size.width,
                    y = (1f - (value - min) / range) * size.height,
                )
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            drawPath(path, colors[seriesIndex], style = Stroke(width = 1.5f))
        }
    }
}

private fun List<Float>.normalizedPercentSeries(): List<Float> {
    val first = firstOrNull()?.takeIf { kotlin.math.abs(it) > 0.0000001f } ?: return this
    return map { ((it - first) / first) * 100f }
}

@Composable
internal fun compareOverlayColor(index: Int, kind: CurrencyKind?): Color {
    val colors = listOf(FxTheme.colors.accent, FxTheme.colors.up, FxTheme.colors.down, FxTheme.colors.textDim)
    return if (kind == CurrencyKind.Crypto) FxTheme.colors.crypto else colors[index % colors.size]
}
