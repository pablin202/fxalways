package com.fxalways.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.designsystem.theme.FxTheme
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

enum class CurrencyKind { Fiat, Crypto }
enum class PillVariant { Accent, Up, Down, Ghost }
enum class Period { OneDay, OneWeek, OneMonth, OneYear, All }

data class FxRate(
    val code: String,
    val name: String,
    val glyph: String,
    val kind: CurrencyKind = CurrencyKind.Fiat,
    val rate: Double,
    val change24h: Double,
    val sparkline: List<Float>,
    val caption: String = "1 USD = ${formatRate(rate)} $code",
)

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(FxTheme.shapes.card)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.card)
            .padding(padding),
        content = content,
    )
}

@Composable
fun BentoTile(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(FxTheme.shapes.tile)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.tile)
            .padding(padding),
        content = content,
    )
}

@Composable
fun GridBg(
    modifier: Modifier = Modifier,
    radialMask: Boolean = true,
) {
    val color = FxTheme.colors.border
    val glowColor = FxTheme.colors.accentSoft
    Canvas(modifier = modifier) {
        val step = 24.dp.toPx()
        fun maskAlpha(point: Offset): Float {
            if (!radialMask) return 1f
            val center = Offset(size.width / 2f, size.height / 2f)
            val dx = point.x - center.x
            val dy = point.y - center.y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            val inner = size.minDimension * 0.12f
            val outer = size.minDimension * 0.48f
            return (1f - ((distance - inner) / (outer - inner))).coerceIn(0f, 1f)
        }
        var x = 0f
        while (x <= size.width) {
            val segments = 40
            repeat(segments) { index ->
                val y0 = (index / segments.toFloat()) * size.height
                val y1 = ((index + 1) / segments.toFloat()) * size.height
                val alpha = maskAlpha(Offset(x, (y0 + y1) / 2f))
                if (alpha > 0.01f) {
                    drawLine(color.copy(alpha = color.alpha * alpha), Offset(x, y0), Offset(x, y1), strokeWidth = 1f)
                }
            }
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            val segments = 40
            repeat(segments) { index ->
                val x0 = (index / segments.toFloat()) * size.width
                val x1 = ((index + 1) / segments.toFloat()) * size.width
                val alpha = maskAlpha(Offset((x0 + x1) / 2f, y))
                if (alpha > 0.01f) {
                    drawLine(color.copy(alpha = color.alpha * alpha), Offset(x0, y), Offset(x1, y), strokeWidth = 1f)
                }
            }
            y += step
        }
        if (radialMask) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.38f,
                    tileMode = TileMode.Clamp,
                ),
                radius = size.minDimension * 0.38f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }
    }
}

@Composable
fun LiveDot(modifier: Modifier = Modifier, color: Color = FxTheme.colors.accent) {
    val transition = rememberInfiniteTransition(label = "live-dot")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "live-dot-alpha",
    )
    Box(
        modifier = modifier
            .size(12.dp)
            .background(color.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(6.dp).alpha(alpha).background(color, CircleShape))
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = FxTheme.colors.textFaint) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = FxTheme.typography.eyebrow,
        color = color,
    )
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.Ghost,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = FxTheme.colors
    val (fg, bg) = when (variant) {
        PillVariant.Accent -> colors.accent to colors.accentSoft
        PillVariant.Up -> colors.up to colors.upSoft
        PillVariant.Down -> colors.down to colors.downSoft
        PillVariant.Ghost -> colors.textDim to colors.surface2
    }
    Row(
        modifier = modifier
            .clip(FxTheme.shapes.pill)
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()
        Text(text, style = FxTheme.typography.pill, color = fg, maxLines = 1)
    }
}

@Composable
fun FlagDot(
    glyph: String,
    kind: CurrencyKind = CurrencyKind.Fiat,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier,
) {
    val colors = FxTheme.colors
    val bg = if (kind == CurrencyKind.Crypto) colors.surface3 else colors.surface2
    val tint = if (kind == CurrencyKind.Crypto) colors.crypto else colors.text
    val style = if (kind == CurrencyKind.Crypto) {
        FxTheme.typography.numberL.copy(fontSize = (size.value * 0.46f).sp)
    } else {
        FxTheme.typography.body.copy(fontSize = (size.value * 0.55f).sp)
    }
    Box(
        modifier = modifier
            .size(size)
            .background(bg, CircleShape)
            .border(1.dp, colors.border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = style, color = tint, textAlign = TextAlign.Center)
    }
}

@Composable
fun SparkLine(
    values: List<Float>,
    modifier: Modifier = Modifier.size(80.dp, 28.dp),
    color: Color? = null,
    showArea: Boolean = true,
    showLastDot: Boolean = false,
) {
    val colors = FxTheme.colors
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val stroke = color ?: if (values.last() >= values.first()) colors.up else colors.down
        val min = values.minOrNull() ?: return@Canvas
        val max = values.maxOrNull() ?: return@Canvas
        val range = (max - min).coerceAtLeast(1e-9f)
        val pad = 2f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val points = values.mapIndexed { index, value ->
            Offset(
                x = pad + (index.toFloat() / values.lastIndex) * w,
                y = pad + (1f - (value - min) / range) * h,
            )
        }
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        if (showArea) {
            val area = Path().apply {
                addPath(line)
                lineTo(points.last().x, size.height - pad)
                lineTo(pad, size.height - pad)
                close()
            }
            drawPath(
                area,
                Brush.verticalGradient(0f to stroke.copy(alpha = 0.30f), 1f to stroke.copy(alpha = 0f)),
            )
        }
        drawPath(line, stroke, style = Stroke(width = 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (showLastDot) drawCircle(stroke, radius = 2.2f, center = points.last())
    }
}

@Composable
fun CurrencyRow(
    rate: FxRate,
    modifier: Modifier = Modifier,
    dense: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val colors = FxTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.78f)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = if (dense) 10.dp else 14.dp),
    ) {
        FlagDot(rate.glyph, rate.kind, size = if (dense) 28.dp else 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rate.code, style = FxTheme.typography.bodyStrong, color = colors.text)
                Text(rate.name, style = FxTheme.typography.caption, color = colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            Text(rate.caption, style = FxTheme.typography.captionMono, color = colors.textDim, maxLines = 1)
        }
        SparkLine(values = rate.sparkline, modifier = Modifier.size(56.dp, 24.dp))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 58.dp)) {
            Text(formatRate(rate.rate), style = FxTheme.typography.numberL, color = colors.text)
            Spacer(Modifier.height(2.dp))
            Text(formatChange(rate.change24h), style = FxTheme.typography.captionMono, color = if (rate.change24h >= 0) colors.up else colors.down)
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    val colors = FxTheme.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(label, style = FxTheme.typography.caption, color = colors.textDim)
        Canvas(Modifier.weight(1f).height(1.dp).padding(horizontal = 10.dp)) {
            var x = 0f
            while (x < size.width) {
                drawLine(colors.border, Offset(x, 0f), Offset((x + 4.dp.toPx()).coerceAtMost(size.width), 0f), strokeWidth = 1f)
                x += 8.dp.toPx()
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = FxTheme.typography.numberBody, color = colors.text)
            if (subtitle != null) Text(subtitle, style = FxTheme.typography.captionMono, color = colors.textFaint)
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    subtitle: String? = null,
    right: (@Composable () -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            if (sub != null) {
                Eyebrow(sub)
                Spacer(Modifier.height(8.dp))
            }
            Text(title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }
        right?.invoke()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, right: String? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Eyebrow(text)
        if (right != null) Text(right, style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
    }
}

@Composable
fun BigValueText(value: String, code: String? = null, modifier: Modifier = Modifier, color: Color = FxTheme.colors.text) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(value, style = FxTheme.typography.numberXL, color = color)
        if (code != null) {
            Spacer(Modifier.width(8.dp))
            Text(code, style = FxTheme.typography.numberBody, color = FxTheme.colors.textDim, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

@Composable
fun PriceChart(data: List<Float>, modifier: Modifier = Modifier.fillMaxWidth().height(200.dp), focusIndex: Int? = null) {
    val colors = FxTheme.colors
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val padX = 8.dp.toPx()
        val padTop = 16.dp.toPx()
        val padBottom = 18.dp.toPx()
        val chartW = size.width - padX * 2
        val chartH = size.height - padTop - padBottom
        repeat(4) { i ->
            val y = padTop + chartH * (i / 3f)
            var x = padX
            while (x < size.width - padX) {
                drawLine(colors.border, Offset(x, y), Offset((x + 4.dp.toPx()).coerceAtMost(size.width - padX), y), strokeWidth = 1f)
                x += 8.dp.toPx()
            }
        }
        val min = data.minOrNull() ?: return@Canvas
        val max = data.maxOrNull() ?: return@Canvas
        val range = (max - min).coerceAtLeast(1e-9f)
        val points = data.mapIndexed { index, value ->
            Offset(
                padX + (index.toFloat() / data.lastIndex) * chartW,
                padTop + (1f - (value - min) / range) * chartH,
            )
        }
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height - padBottom)
            lineTo(padX, size.height - padBottom)
            close()
        }
        drawPath(area, Brush.verticalGradient(0f to colors.accent.copy(alpha = 0.22f), 1f to colors.accent.copy(alpha = 0f)))
        drawPath(line, colors.accent, style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (focusIndex != null) {
            val focus = focusIndex.coerceIn(0, data.lastIndex)
            val p = points[focus]
            var y = padTop
            while (y < size.height - padBottom) {
                drawLine(colors.accent, Offset(p.x, y), Offset(p.x, (y + 3.dp.toPx()).coerceAtMost(size.height - padBottom)), strokeWidth = 0.9f)
                y += 6.dp.toPx()
            }
            drawCircle(colors.accent.copy(alpha = 0.18f), radius = 9.dp.toPx(), center = p)
            drawCircle(colors.accent, radius = 4.5.dp.toPx(), center = p)
        }
    }
}

@Composable
fun FxBottomBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dividerColor = FxTheme.colors.border.copy(alpha = 0.55f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(FxTheme.shapes.field)
                    .background(if (selected) FxTheme.colors.surface1 else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                BottomTabIcon(label, if (selected) FxTheme.colors.accent else FxTheme.colors.textFaint)
                Text(label.uppercase(), style = FxTheme.typography.tab, color = if (selected) FxTheme.colors.accent else FxTheme.colors.textFaint)
            }
        }
    }
}

@Composable
private fun BottomTabIcon(label: String, color: Color) {
    Canvas(Modifier.size(28.dp)) {
        val stroke = 1.8.dp.toPx()
        val dot = 2.2.dp.toPx()
        when (label.lowercase()) {
            "rates" -> {
                val points = listOf(
                    Offset(size.width * 0.12f, size.height * 0.68f),
                    Offset(size.width * 0.34f, size.height * 0.50f),
                    Offset(size.width * 0.56f, size.height * 0.58f),
                    Offset(size.width * 0.84f, size.height * 0.28f),
                )
                points.zipWithNext().forEach { (from, to) ->
                    drawLine(color, from, to, strokeWidth = stroke, cap = StrokeCap.Round)
                }
                drawCircle(color, dot, points.last())
            }
            "convert" -> {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.34f), Offset(size.width * 0.78f, size.height * 0.34f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.66f, size.height * 0.22f), Offset(size.width * 0.78f, size.height * 0.34f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.66f, size.height * 0.46f), Offset(size.width * 0.78f, size.height * 0.34f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.82f, size.height * 0.66f), Offset(size.width * 0.22f, size.height * 0.66f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.54f), Offset(size.width * 0.22f, size.height * 0.66f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.78f), Offset(size.width * 0.22f, size.height * 0.66f), strokeWidth = stroke, cap = StrokeCap.Round)
            }
            "compare" -> {
                val barWidth = size.width * 0.14f
                listOf(0.34f, 0.54f, 0.24f).forEachIndexed { index, top ->
                    val left = size.width * (0.20f + index * 0.25f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, size.height * top),
                        size = Size(barWidth, size.height * (0.80f - top)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f),
                    )
                }
            }
            "news" -> {
                drawRect(color, topLeft = Offset(size.width * 0.18f, size.height * 0.18f), size = Size(size.width * 0.64f, size.height * 0.64f), style = Stroke(width = stroke))
                repeat(3) { index ->
                    val y = size.height * (0.36f + index * 0.16f)
                    drawLine(color, Offset(size.width * 0.30f, y), Offset(size.width * 0.70f, y), strokeWidth = stroke, cap = StrokeCap.Round)
                }
            }
            else -> {
                drawCircle(color, dot, Offset(size.width * 0.28f, size.height * 0.50f))
                drawCircle(color, dot, Offset(size.width * 0.50f, size.height * 0.50f))
                drawCircle(color, dot, Offset(size.width * 0.72f, size.height * 0.50f))
            }
        }
    }
}

@Composable
fun MetricTile(label: String, value: String, sub: String? = null, modifier: Modifier = Modifier) {
    BentoTile(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Eyebrow(label)
            Text(value, style = FxTheme.typography.numberL, color = FxTheme.colors.text)
            if (sub != null) Text(sub, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
fun SegmentedPeriods(selected: Period, onSelected: (Period) -> Unit, modifier: Modifier = Modifier) {
    val periods = listOf(Period.OneDay to "1D", Period.OneWeek to "1W", Period.OneMonth to "1M", Period.OneYear to "1Y", Period.All to "ALL")
    Row(modifier.clip(FxTheme.shapes.field).background(FxTheme.colors.surface2).padding(3.dp)) {
        periods.forEach { (period, label) ->
            val active = period == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(FxTheme.shapes.field)
                    .background(if (active) FxTheme.colors.bg else Color.Transparent)
                    .border(if (active) 1.dp else 0.dp, if (active) FxTheme.colors.accentLine else Color.Transparent, FxTheme.shapes.field)
                    .clickable(enabled = !active) { onSelected(period) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = FxTheme.typography.pill, color = if (active) FxTheme.colors.accent else FxTheme.colors.textDim)
            }
        }
    }
}

fun formatRate(n: Double): String {
    val decimals = when {
        abs(n) >= 1000 -> 2
        abs(n) >= 100 -> 3
        abs(n) >= 1 -> 4
        abs(n) >= 0.01 -> 4
        else -> 7
    }
    return n.fixed(decimals, useGrouping = true)
}

fun formatChange(change: Double): String =
    "${if (change >= 0) "+" else ""}${change.fixed(2)}%"

private fun Double.fixed(decimals: Int, useGrouping: Boolean = false): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = round(abs(this) * multiplier).toLong()
    val whole = rounded / multiplier.toLong()
    val fraction = (rounded % multiplier.toLong()).toString().padStart(decimals, '0')
    val groupedWhole = if (useGrouping) whole.toString().withThousands() else whole.toString()
    val sign = if (this < 0) "-" else ""
    return "$sign$groupedWhole.$fraction"
}

private fun String.withThousands(): String =
    reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
