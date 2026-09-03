package com.fxalways.app.screens.more

import com.fxalways.app.screens.*
import com.fxalways.app.screens.paywall.localizedProStatusLabel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun MoreScreen(
    subscriptionState: SubscriptionState,
    alertsCount: Int,
    watchlistCount: Int,
    onOpenAlerts: () -> Unit,
    onOpenWatchlist: () -> Unit,
    onOpenTraveler: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenCompare: () -> Unit = {},
    onOpenCrypto: () -> Unit = {},
) {
    ScreenScaffold {
        ScreenHeader(ui("More"), sub = ui("TOOLS"), subtitle = ui("Traveler, markets, watchlist and account"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MoreRow(
                    icon = MoreFeatureIcon.Traveler,
                    title = ui("Traveler"),
                    subtitle = ui("Tipping, cash/card and local prices"),
                    tag = "more_traveler",
                    onClick = onOpenTraveler,
                )
                MoreRow(
                    icon = MoreFeatureIcon.News,
                    title = ui("News"),
                    subtitle = ui("Market context when you need it"),
                    tag = "more_news",
                    onClick = onOpenNews,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Watchlist,
                    title = ui("Compare currencies"),
                    subtitle = ui("Multi-currency board and 30-day ranges"),
                    tag = "more_compare",
                    onClick = onOpenCompare,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Watchlist,
                    title = ui("Crypto"),
                    subtitle = ui("BTC, ETH and stablecoins board"),
                    tag = "more_crypto",
                    onClick = onOpenCrypto,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Alerts,
                    title = ui("Alerts"),
                    subtitle = "$alertsCount ${ui("active")} · ${ui("know when a rate is worth acting on")}",
                    tag = "more_alerts",
                    onClick = onOpenAlerts,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Watchlist,
                    title = ui("Watchlist"),
                    subtitle = "$watchlistCount ${ui("currencies")} · ${ui("track the currencies you care about")}",
                    tag = "more_watchlist",
                    onClick = onOpenWatchlist,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Settings,
                    title = ui("Settings"),
                    subtitle = ui("Account, language, providers and widgets"),
                    tag = "more_settings",
                    onClick = onOpenSettings,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Pro,
                    title = if (subscriptionState.isPremium) ui("FX/ Pro active") else ui("Upgrade to Pro"),
                    subtitle = if (subscriptionState.isPremium) {
                        subscriptionState.localizedProStatusLabel()
                    } else {
                        ui("Unlock OCR, alerts and full provider comparison")
                    },
                    tag = "more_pro",
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
internal fun MoreRow(
    icon: MoreFeatureIcon,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(FxTheme.shapes.icon)
                .background(FxTheme.colors.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            MoreFeatureIconView(icon)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}

internal enum class MoreFeatureIcon {
    Traveler,
    News,
    Alerts,
    Watchlist,
    Settings,
    Pro,
}

@Composable
internal fun MoreFeatureIconView(icon: MoreFeatureIcon) {
    val accent = FxTheme.colors.accent
    Canvas(Modifier.size(30.dp)) {
        val lineWidth = 2.2.dp.toPx()
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        val thinStroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        fun iconLine(startX: Float, startY: Float, endX: Float, endY: Float, strokeWidth: Float = lineWidth) {
            drawLine(
                color = accent,
                start = Offset(w * startX, h * startY),
                end = Offset(w * endX, h * endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        when (icon) {
            MoreFeatureIcon.Traveler -> {
                iconLine(0.34f, 0.22f, 0.66f, 0.22f)
                iconLine(0.34f, 0.22f, 0.34f, 0.32f)
                iconLine(0.66f, 0.22f, 0.66f, 0.32f)
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.22f, h * 0.32f),
                    size = Size(w * 0.56f, h * 0.46f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke,
                )
                iconLine(0.36f, 0.44f, 0.36f, 0.66f, thinStroke.width)
                iconLine(0.64f, 0.44f, 0.64f, 0.66f, thinStroke.width)
            }
            MoreFeatureIcon.News -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.24f, h * 0.16f),
                    size = Size(w * 0.52f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                    style = stroke,
                )
                iconLine(0.34f, 0.36f, 0.66f, 0.36f, thinStroke.width)
                iconLine(0.34f, 0.50f, 0.66f, 0.50f, thinStroke.width)
                iconLine(0.34f, 0.64f, 0.56f, 0.64f, thinStroke.width)
            }
            MoreFeatureIcon.Alerts -> {
                val bell = Path().apply {
                    moveTo(w * 0.31f, h * 0.60f)
                    quadraticTo(w * 0.32f, h * 0.34f, w * 0.50f, h * 0.30f)
                    quadraticTo(w * 0.68f, h * 0.34f, w * 0.69f, h * 0.60f)
                    lineTo(w * 0.76f, h * 0.70f)
                    lineTo(w * 0.24f, h * 0.70f)
                    close()
                }
                drawPath(bell, accent, style = stroke)
                iconLine(0.45f, 0.22f, 0.55f, 0.22f)
                drawArc(accent, 15f, 150f, false, Offset(w * 0.42f, h * 0.68f), Size(w * 0.16f, h * 0.16f), style = thinStroke)
            }
            MoreFeatureIcon.Watchlist -> {
                drawCircle(accent, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                val chart = Path().apply {
                    moveTo(w * 0.30f, h * 0.58f)
                    lineTo(w * 0.43f, h * 0.46f)
                    lineTo(w * 0.52f, h * 0.54f)
                    lineTo(w * 0.70f, h * 0.36f)
                }
                drawPath(chart, accent, style = thinStroke)
                drawCircle(accent, radius = w * 0.035f, center = Offset(w * 0.70f, h * 0.36f))
            }
            MoreFeatureIcon.Settings -> {
                drawCircle(accent, radius = w * 0.17f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                listOf(
                    Offset(w * 0.50f, h * 0.18f) to Offset(w * 0.50f, h * 0.28f),
                    Offset(w * 0.50f, h * 0.72f) to Offset(w * 0.50f, h * 0.82f),
                    Offset(w * 0.18f, h * 0.50f) to Offset(w * 0.28f, h * 0.50f),
                    Offset(w * 0.72f, h * 0.50f) to Offset(w * 0.82f, h * 0.50f),
                    Offset(w * 0.28f, h * 0.28f) to Offset(w * 0.35f, h * 0.35f),
                    Offset(w * 0.65f, h * 0.65f) to Offset(w * 0.72f, h * 0.72f),
                    Offset(w * 0.72f, h * 0.28f) to Offset(w * 0.65f, h * 0.35f),
                    Offset(w * 0.35f, h * 0.65f) to Offset(w * 0.28f, h * 0.72f),
                ).forEach { (start, end) ->
                    drawLine(accent, start, end, strokeWidth = lineWidth, cap = StrokeCap.Round)
                }
            }
            MoreFeatureIcon.Pro -> {
                drawCircle(accent, radius = w * 0.18f, center = Offset(w * 0.36f, h * 0.50f), style = stroke)
                drawCircle(accent, radius = w * 0.18f, center = Offset(w * 0.64f, h * 0.50f), style = stroke)
                iconLine(0.44f, 0.38f, 0.56f, 0.62f)
                iconLine(0.44f, 0.62f, 0.56f, 0.38f)
            }
        }
    }
}
