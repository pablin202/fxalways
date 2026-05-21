package com.fxalways.app.screens.alerts

import androidx.compose.runtime.Composable
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.matchesDefinition
import com.fxalways.app.screens.formatPercentValue
import com.fxalways.app.screens.formatSignedPercent
import com.fxalways.app.screens.ui
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.featureAccess
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.formatRate

internal val AlertKind.label: String
    get() = when (this) {
        AlertKind.Target -> "Target"
        AlertKind.DailyChange -> "Daily move"
    }

internal fun AlertDirection.label(kind: AlertKind): String =
    when (kind) {
        AlertKind.Target -> when (this) {
            AlertDirection.Above -> "Above"
            AlertDirection.Below -> "Below"
        }
        AlertKind.DailyChange -> when (this) {
            AlertDirection.Above -> "Up"
            AlertDirection.Below -> "Down"
        }
    }

enum class QuickAlertState(
    val label: String,
    val variant: PillVariant,
) {
    Create("create", PillVariant.Ghost),
    Active("active", PillVariant.Up),
    Paused("resume", PillVariant.Ghost),
    Locked("pro", PillVariant.Accent),
}

internal data class AlertPreset(
    val label: String,
    val percent: Double,
)

internal data class AlertTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: AlertKind,
    val direction: AlertDirection,
    val targetText: (FxRate) -> String,
)

internal val alertTemplates = listOf(
    AlertTemplate(
        id = "travel_good_rate",
        title = "Good travel rate",
        subtitle = "Alert when the destination rate improves for a trip.",
        kind = AlertKind.Target,
        direction = AlertDirection.Above,
        targetText = { rate -> formatRate(rate.rate * 1.01) },
    ),
    AlertTemplate(
        id = "daily_breakout",
        title = "Daily breakout",
        subtitle = "Alert when a pair moves sharply in one day.",
        kind = AlertKind.DailyChange,
        direction = AlertDirection.Above,
        targetText = { "2.0" },
    ),
    AlertTemplate(
        id = "remittance_window",
        title = "Better remittance window",
        subtitle = "Alert before a repeat transfer window improves.",
        kind = AlertKind.Target,
        direction = AlertDirection.Below,
        targetText = { rate -> formatRate(rate.rate * 0.99) },
    ),
)

internal val alertPresets = listOf(
    AlertPreset("-1%", -1.0),
    AlertPreset("-0.5%", -0.5),
    AlertPreset("+0.5%", 0.5),
    AlertPreset("+1%", 1.0),
)

internal fun smartAlertSuggestions(rates: List<FxRate>, isPremium: Boolean): List<SmartAlertSuggestion> =
    rates
        .mapNotNull(::smartAlertSuggestion)
        .sortedWith(compareByDescending<SmartAlertSuggestion> { it.strength }.thenBy { it.rate.code })
        .take(if (isPremium) 4 else 2)

private fun smartAlertSuggestion(rate: FxRate): SmartAlertSuggestion? {
    val points = (rate.sparkline + rate.rate.toFloat())
        .filter { it.isFinite() && it > 0f }
        .map { it.toDouble() }
    if (points.size < 3) return null
    val low = points.minOrNull() ?: return null
    val high = points.maxOrNull() ?: return null
    val range = high - low
    if (range <= 0.0) return null
    val position = ((rate.rate - low) / range).coerceIn(0.0, 1.0)
    return when {
        position >= 0.74 -> SmartAlertSuggestion(
            rate = rate,
            title = "Near recent high",
            subtitle = "30d range signal",
            target = rate.rate * 1.002,
            direction = AlertDirection.Above,
            strength = position,
        )
        position <= 0.26 -> SmartAlertSuggestion(
            rate = rate,
            title = "Near recent low",
            subtitle = "30d range signal",
            target = rate.rate * 0.998,
            direction = AlertDirection.Below,
            strength = 1.0 - position,
        )
        else -> null
    }
}

internal fun List<PriceAlert>.findQuickAlert(baseCurrency: String, rate: FxRate): PriceAlert? {
    val target = quickAlertTarget(rate)
    return findMatchingAlert(
        baseCurrency = baseCurrency,
        quote = rate.code,
        target = target,
        direction = AlertDirection.Above,
        kind = AlertKind.Target,
    )
}

private fun quickAlertTarget(rate: FxRate): Double =
    rate.rate * 1.01

internal fun List<PriceAlert>.findMatchingAlert(
    baseCurrency: String,
    quote: String,
    target: Double,
    direction: AlertDirection,
    kind: AlertKind,
): PriceAlert? =
    firstOrNull {
        it.matchesDefinition(
            base = baseCurrency,
            quote = quote,
            target = target,
            direction = direction,
            kind = kind,
        )
    }

internal fun PriceAlert.isHit(currentRate: Double?, currentChangePct: Double?): Boolean =
    when (kind) {
        AlertKind.Target -> {
            if (currentRate == null) false else when (direction) {
                AlertDirection.Above -> currentRate >= target
                AlertDirection.Below -> currentRate <= target
            }
        }
        AlertKind.DailyChange -> {
            if (currentChangePct == null) false else when (direction) {
                AlertDirection.Above -> currentChangePct >= target
                AlertDirection.Below -> currentChangePct <= -target
            }
        }
    }

internal fun PriceAlert.targetLabel(): String =
    when (kind) {
        AlertKind.Target -> formatRate(target)
        AlertKind.DailyChange -> "${formatPercentValue(target)}%"
    }

private fun PriceAlert.dailyChangeDistancePercent(currentChangePct: Double): String {
    val threshold = if (direction == AlertDirection.Above) target else -target
    val distance = kotlin.math.abs(threshold - currentChangePct).coerceAtLeast(0.0)
    return if (distance < 0.1) "<0.1" else formatPercentValue(distance)
}

private fun PriceAlert.distancePercent(currentRate: Double): String {
    val distance = when (direction) {
        AlertDirection.Above -> (target - currentRate) / currentRate
        AlertDirection.Below -> (currentRate - target) / currentRate
    }.coerceAtLeast(0.0) * 100.0
    return if (distance < 0.1) "<0.1" else ((distance * 10).toInt() / 10.0).toString()
}

internal fun defaultAlertInput(rate: FxRate, direction: AlertDirection, kind: AlertKind): String =
    when (kind) {
        AlertKind.Target -> {
            val multiplier = if (direction == AlertDirection.Above) 1.01 else 0.99
            formatRate(rate.rate * multiplier)
        }
        AlertKind.DailyChange -> "1.0"
    }

@Composable
internal fun localizedAlertStatusLabel(alert: PriceAlert, currentRate: Double?, currentChangePct: Double?): String =
    when {
        alert.kind == AlertKind.Target && currentRate == null -> "${ui("waiting for live rate")} · ${alert.base}"
        alert.kind == AlertKind.DailyChange && currentChangePct == null -> ui("waiting for 24h change")
        alert.isHit(currentRate, currentChangePct) -> ui("target hit")
        alert.kind == AlertKind.Target && currentRate != null -> "${alert.distancePercent(currentRate)}% ${ui("away")}"
        alert.kind == AlertKind.DailyChange && currentChangePct != null -> "${alert.dailyChangeDistancePercent(currentChangePct)} ${ui("pts away")}"
        else -> ui("waiting")
    }

@Composable
internal fun localizedAlertDistanceLabel(alert: PriceAlert, currentRate: Double?, currentChangePct: Double?): String =
    when {
        alert.kind == AlertKind.Target && currentRate == null -> ui("base changed")
        alert.kind == AlertKind.DailyChange && currentChangePct == null -> ui("waiting")
        alert.isHit(currentRate, currentChangePct) -> ui("target reached")
        alert.kind == AlertKind.Target && currentRate != null -> "${alert.distancePercent(currentRate)}% ${ui("to target")}"
        alert.kind == AlertKind.DailyChange && currentChangePct != null -> "${alert.dailyChangeDistancePercent(currentChangePct)} ${ui("pts to move")}"
        else -> ui("waiting")
    }

@Composable
internal fun localizedAlertSummaryLine(
    kind: AlertKind,
    rate: FxRate,
    direction: AlertDirection,
    targetValue: Double,
    currentChangePct: Double,
): String =
    when (kind) {
        AlertKind.Target -> "${ui("Current")} ${formatRate(rate.rate)} · ${ui("target")} ${if (targetValue > 0.0) formatRate(targetValue) else "--"}"
        AlertKind.DailyChange -> {
            val threshold = if (targetValue > 0.0) {
                "${ui(direction.label(kind)).lowercase()} ${formatPercentValue(targetValue)}%"
            } else {
                "--"
            }
            "24h ${formatSignedPercent(currentChangePct)} · ${ui("alert at")} $threshold"
        }
    }

internal fun canCreateAlert(subscriptionState: SubscriptionState, currentCount: Int): Boolean {
    val access = subscriptionState.featureAccess()
    return access.hasUnlimitedAlerts || currentCount < access.alertLimit
}
