package com.fxalways.app.screens.detail

import com.fxalways.app.screens.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.LiveRatesState
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun RateTrustCard(
    liveState: LiveRatesState,
    modifier: Modifier = Modifier,
    providerOverride: String? = null,
    updatedOverride: String? = null,
) {
    val loading = liveState.isInitialRateLoading()
    val source = providerOverride?.takeIf { it.isNotBlank() } ?: liveState.rateProviderLabel()
    val updated = updatedOverride?.takeIf { it.isNotBlank() } ?: liveState.updatedLabel
    val status = when {
        loading -> ui("Loading")
        liveState.isOfflineCache -> ui("Cached")
        liveState.isLive -> ui("Live")
        else -> ui("Preview")
    }
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("RATE TRUST"), color = FxTheme.colors.accent)
                Pill(status, variant = if (liveState.isLive) PillVariant.Accent else PillVariant.Ghost)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (loading) {
                    TrustMetricSkeleton(ui("Source"), Modifier.weight(1f).testTag("rate_trust_source_loading"))
                    TrustMetricSkeleton(ui("Updated"), Modifier.weight(1f).testTag("rate_trust_updated_loading"))
                } else {
                    TrustMetric(ui("Source"), compactProviderLabel(source), Modifier.weight(1f).testTag("rate_trust_source"))
                    TrustMetric(ui("Updated"), compactRuntimeLabel(updated), Modifier.weight(1f).testTag("rate_trust_updated"))
                }
            }
            Text(
                ui("Indicative mid-market rates. Final transfer or card rates can include provider fees and markups."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
        }
    }
}

@Composable
private fun TrustMetricSkeleton(label: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "trustSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "trustSkeletonAlpha",
    )
    Column(
        modifier = modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.6f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(label.uppercase(), style = FxTheme.typography.eyebrow, color = FxTheme.colors.textFaint, maxLines = 1)
        Box(
            Modifier
                .fillMaxWidth(0.84f)
                .height(14.dp)
                .clip(FxTheme.shapes.field)
                .background(FxTheme.colors.surface3.copy(alpha = alpha)),
        )
        Box(
            Modifier
                .fillMaxWidth(0.58f)
                .height(10.dp)
                .clip(FxTheme.shapes.field)
                .background(FxTheme.colors.surface3.copy(alpha = alpha * 0.78f)),
        )
    }
}

@Composable
private fun TrustMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.6f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label.uppercase(), style = FxTheme.typography.eyebrow, color = FxTheme.colors.textFaint, maxLines = 1)
        Text(value, style = FxTheme.typography.captionMono, color = FxTheme.colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun RateTrustDetailsCard(
    liveState: LiveRatesState,
    modifier: Modifier = Modifier,
) {
    val loading = liveState.isInitialRateLoading()
    val decisionGrade = when {
        loading -> "Loading"
        liveState.isLive -> "Live"
        else -> "Cached"
    }
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("TRUST DETAILS"), color = FxTheme.colors.accent)
                Pill(ui(decisionGrade), variant = if (liveState.isLive) PillVariant.Up else PillVariant.Ghost)
            }
            if (loading) {
                InlineSkeletonRows(
                    rows = 4,
                    modifier = Modifier.testTag("trust_details_loading_skeleton"),
                )
            } else {
                KeyValueRow(
                    ui("Decision grade"),
                    ui(decisionGrade),
                    "${ui("Source")} ${compactProviderLabel(liveState.rateProviderLabel())} · ${ui("Updated")} ${compactRuntimeLabel(liveState.updatedLabel)}",
                    modifier = Modifier.testTag("trust_decision_grade"),
                )
                KeyValueRow(
                    ui("Provider rates can differ"),
                    ui("Fees + spread"),
                    ui("We use mid-market rates for intelligence; providers can add fees, spread, delivery limits and card/cash markups."),
                    modifier = Modifier.testTag("trust_provider_disclaimer"),
                )
            }
        }
    }
}

@Composable
internal fun LoadingSkeletonCard(title: String, rows: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    BentoCard(modifier, padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(title, color = FxTheme.colors.accent)
            repeat(rows) { index ->
                Box(
                    Modifier
                        .fillMaxWidth(if (index % 2 == 0) 1f else 0.78f)
                        .height(14.dp)
                        .clip(FxTheme.shapes.field)
                        .background(FxTheme.colors.surface3.copy(alpha = alpha)),
                )
            }
        }
    }
}

@Composable
internal fun InlineSkeletonRows(rows: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "inlineSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "inlineSkeletonAlpha",
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { index ->
            Box(
                Modifier
                    .fillMaxWidth(if (index % 2 == 0) 1f else 0.68f)
                    .height(14.dp)
                    .clip(FxTheme.shapes.field)
                    .background(FxTheme.colors.surface3.copy(alpha = alpha)),
            )
        }
    }
}

internal fun LiveRatesState.rateProviderLabel(): String {
    val parts = updatedLabel.split("·").map { it.trim() }.filter { it.isNotBlank() }
    return parts.getOrNull(1)
        ?.takeUnless { it.contains("refreshed", ignoreCase = true) || it.contains("cached", ignoreCase = true) }
        ?: if (crypto.isNotEmpty()) "FX backend / CoinPaprika" else "FX backend"
}

internal fun compactProviderLabel(label: String): String =
    when {
        label.contains("Frankfurter", ignoreCase = true) ||
            label.contains("European Central Bank", ignoreCase = true) -> "ECB / Frankfurter"
        label.contains("CoinPaprika", ignoreCase = true) -> "FX / CoinPaprika"
        label.length > 24 -> label.take(21).trimEnd() + "..."
        else -> label
    }

@Composable
internal fun compactRuntimeLabel(label: String): String {
    val localized = localizedRuntimeLabel(label)
    return when {
        localized == ui("loading") -> ui("loading")
        label.contains("Frankfurter", ignoreCase = true) ||
            label.contains("European Central Bank", ignoreCase = true) -> {
            val date = label.substringBefore("·").trim().takeIf { it.isNotBlank() }
            val refreshed = label.substringAfterLast("refreshed", "").trim()
            listOfNotNull(date, refreshed.takeIf { it.isNotBlank() }?.let { "${ui("refreshed")} $it" })
                .joinToString(" · ")
                .ifBlank { "ECB" }
        }
        localized.length > 34 -> localized.take(31).trimEnd() + "..."
        else -> localized
    }
}

internal fun LiveRatesState.isInitialRateLoading(): Boolean =
    isLoading && !isLive && !isOfflineCache
