package com.fxalways.app.screens.detail

import com.fxalways.app.screens.*
import com.fxalways.app.screens.more.MoreFeatureIcon
import com.fxalways.app.screens.more.MoreFeatureIconView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.PriceChart
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun ShareRateCard(
    baseCurrency: String,
    rate: FxRate,
    provider: String,
    updatedLabel: String,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(baseCurrency, rate.code, rate.rate, provider, updatedLabel) { mutableStateOf(false) }
    val updatedForDisplay = compactRuntimeLabel(updatedLabel)
    val shareText = remember(baseCurrency, rate, provider, updatedForDisplay) {
        buildString {
            append("FX Always rate card\n")
            append("$baseCurrency / ${rate.code}: ${formatRate(rate.rate)}\n")
            append("24h: ${formatChange(rate.change24h)}\n")
            append("Source: ${compactProviderLabel(provider)}\n")
            append("Updated: $updatedForDisplay\n")
            append("Disclaimer: Indicative only. Check provider fees before sending money.")
        }
    }
    BentoCard(Modifier.testTag("detail_share_rate_card"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(ui("SHARE RATE CARD"), color = FxTheme.colors.accent)
                    Text("$baseCurrency / ${rate.code} · ${formatRate(rate.rate)}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
                        "${ui("Source")} ${compactProviderLabel(provider)} · ${ui("Updated")} ${compactRuntimeLabel(updatedLabel)}",
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("detail_share_rate_source"),
                    )
                }
                Pill(formatChange(rate.change24h), variant = if (rate.change24h >= 0.0) PillVariant.Up else PillVariant.Down)
            }
            Text(
                ui("Indicative only. Check provider fees before sending money."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
                modifier = Modifier.testTag("detail_share_disclaimer"),
            )
            GhostButton(
                text = if (copied) ui("Copied rate card") else ui("Copy rate card"),
                modifier = Modifier.fillMaxWidth().testTag("detail_share_copy"),
                onClick = {
                    clipboard.setText(AnnotatedString(shareText))
                    copied = true
                },
            )
        }
    }
}

@Composable
internal fun EconomicCalendarCard(
    rate: FxRate,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val events = remember(rate.code, rate.kind) { economicCalendarEvents(rate) }
    var impactFilter by remember(rate.code, rate.kind) { mutableStateOf("All") }
    val filteredEvents = remember(events, impactFilter, isPremium) {
        if (!isPremium || impactFilter == "All") {
            events
        } else {
            events.filter { it.impact == impactFilter }
        }
    }
    BentoCard(Modifier.testTag("detail_economic_calendar"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "High", "Medium").forEach { option ->
                    val locked = option != "All" && !isPremium
                    Pill(
                        text = if (locked) "${ui(option)} · Pro" else ui(option),
                        variant = if (impactFilter == option && !locked) PillVariant.Accent else PillVariant.Ghost,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_calendar_filter_${option.lowercase()}")
                            .clickable {
                                if (locked) {
                                    onOpenPaywall()
                                } else {
                                    impactFilter = option
                                }
                            },
                    )
                }
            }
            filteredEvents.take(if (isPremium) filteredEvents.size else 2).forEachIndexed { index, event ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .testTag("detail_calendar_event_$index")
                        .clip(FxTheme.shapes.field)
                        .background(FxTheme.colors.surface2)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(ui(event.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${event.day} · ${ui(event.topic)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                    }
                    Pill("${ui("Impact")} ${ui(event.impact)}", variant = event.impactVariant)
                }
            }
            KeyValueRow(
                ui("Calendar plan"),
                if (filteredEvents.any { it.impact == "High" }) ui("Watch high-impact windows") else ui("Low event risk"),
                "${filteredEvents.size} ${ui("events")} · ${ui("Next 7 days")}",
                modifier = Modifier.testTag("detail_calendar_plan"),
            )
            if (!isPremium && events.size > 2) {
                GhostButton(
                    text = ui("Pro unlocks the full calendar and impact filters."),
                    modifier = Modifier.fillMaxWidth().testTag("detail_calendar_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

private data class EconomicCalendarEvent(
    val day: String,
    val title: String,
    val topic: String,
    val impact: String,
    val impactVariant: PillVariant,
)

private fun economicCalendarEvents(rate: FxRate): List<EconomicCalendarEvent> =
    if (rate.kind == CurrencyKind.Crypto) {
        listOf(
            EconomicCalendarEvent("Mon", "${rate.code} liquidity watch", "Liquidity", "Medium", PillVariant.Accent),
            EconomicCalendarEvent("Wed", "Network activity pulse", "Network", "Medium", PillVariant.Accent),
            EconomicCalendarEvent("Fri", "Protocol market update", "Protocol", "Low", PillVariant.Ghost),
        )
    } else {
        listOf(
            EconomicCalendarEvent("Tue", "${rate.code} central bank speaker", "Central bank", "High", PillVariant.Down),
            EconomicCalendarEvent("Wed", "${rate.code} inflation print", "Inflation", "High", PillVariant.Down),
            EconomicCalendarEvent("Thu", "${rate.code} jobs update", "Jobs", "Medium", PillVariant.Accent),
            EconomicCalendarEvent("Fri", "${rate.code} growth tracker", "Growth", "Low", PillVariant.Ghost),
        )
    }

@Composable
internal fun DetailChartLoadingOverlay(data: List<Float>, modifier: Modifier = Modifier) {
    val colors = FxTheme.colors
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1_100), repeatMode = RepeatMode.Restart),
    )
    Box(modifier) {
        PriceChart(data, Modifier.matchParentSize().alpha(0.46f))
        Canvas(Modifier.matchParentSize()) {
            val x = size.width * progress
            drawLine(
                colors.accent.copy(alpha = 0.48f),
                Offset(x, 16.dp.toPx()),
                Offset(x, size.height - 18.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}

@Composable
internal fun DetailEventRow(story: NewsStory, modifier: Modifier = Modifier, onOpenUrl: (String) -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(enabled = story.sourceUrl.isNotBlank()) { onOpenUrl(story.sourceUrl) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, modifier = Modifier.width(58.dp))
        Pill(story.tag, variant = PillVariant.Accent)
        Text(story.title, style = FxTheme.typography.caption, color = FxTheme.colors.text, modifier = Modifier.weight(1f))
        if (story.sourceUrl.isNotBlank()) {
            Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
internal fun EmptyDetailSection(title: String, subtitle: String, modifier: Modifier = Modifier) {
    BentoCard(modifier.fillMaxWidth(), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
internal fun GhostIconButton(
    icon: MoreFeatureIcon,
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit = {},
) {
    Row(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoreFeatureIconView(icon)
        Spacer(Modifier.width(8.dp))
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
    }
}
