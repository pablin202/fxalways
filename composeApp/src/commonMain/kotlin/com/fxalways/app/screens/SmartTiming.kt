package com.fxalways.app.screens

import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.formatRate

internal data class SmartTimingInsight(
    val score: Int,
    val signal: String,
    val action: String,
    val travelAdvice: String,
    val savingsAdvice: String,
    val remittanceAdvice: String,
    val horizons: List<TimingHorizon>,
)

internal data class TimingHorizon(
    val label: String,
    val rangeLabel: String,
    val positionLabel: String,
    val trendLabel: String,
    val volatilityLabel: String,
    val position: Double,
    val trendPct: Double,
    val volatilityPct: Double,
)

internal fun smartTimingInsight(sourceRate: FxRate, targetRate: FxRate): SmartTimingInsight {
    val pairSeries = pairRateSeries(sourceRate, targetRate)
    val horizons = listOf(
        timingHorizon("7D", pairSeries, 7),
        timingHorizon("30D", pairSeries, 30),
        timingHorizon("90D", pairSeries, 90),
    )
    val primary = horizons.first()
    val score = timingScore(primary)
    val signal = when {
        score >= 82 -> "Strong rate"
        score >= 58 -> "Good time"
        else -> "Wait"
    }
    val action = when (signal) {
        "Strong rate" -> "Convert now: the pair is near the top of its recent range."
        "Good time" -> "Convert in tranches: current rate is better than average but not stretched."
        else -> "Wait or set an alert: current rate is below its recent advantage zone."
    }
    return SmartTimingInsight(
        score = score,
        signal = signal,
        action = action,
        travelAdvice = when (signal) {
            "Wait" -> "Cover essentials only"
            "Good time" -> "Buy partial budget"
            else -> "Lock trip cash"
        },
        savingsAdvice = when (signal) {
            "Wait" -> "Use alerts"
            "Good time" -> "Average in"
            else -> "Move larger slice"
        },
        remittanceAdvice = when (signal) {
            "Wait" -> "Delay if flexible"
            "Good time" -> "Send staged"
            else -> "Send now"
        },
        horizons = horizons,
    )
}

private fun pairRateSeries(sourceRate: FxRate, targetRate: FxRate): List<Double> {
    val targetSeries = targetRate.sparkline.ifEmpty { listOf(targetRate.rate.toFloat()) }.map { it.toDouble() }
    val sourceSeries = sourceRate.sparkline.ifEmpty { listOf(sourceRate.rate.toFloat()) }.map { it.toDouble() }
    val points = maxOf(2, targetSeries.size, sourceSeries.size)
    return List(points) { index ->
        val target = targetSeries.valueAtScaledIndex(index, points, targetRate.rate)
        val source = sourceSeries.valueAtScaledIndex(index, points, sourceRate.rate)
        if (source == 0.0) 0.0 else target / source
    }
}

private fun List<Double>.valueAtScaledIndex(index: Int, total: Int, fallback: Double): Double {
    if (isEmpty()) return fallback
    if (size == 1 || total <= 1) return first()
    val scaled = (index.toDouble() / (total - 1).coerceAtLeast(1)) * (size - 1)
    return this[scaled.toInt().coerceIn(0, lastIndex)]
}

private fun timingHorizon(label: String, series: List<Double>, points: Int): TimingHorizon {
    val window = series.takeLast(points.coerceAtMost(series.size)).ifEmpty { series }
    val current = window.lastOrNull() ?: 0.0
    val open = window.firstOrNull() ?: current
    val high = window.maxOrNull() ?: current
    val low = window.minOrNull() ?: current
    val average = window.average().takeIf { !it.isNaN() } ?: current
    val spread = high - low
    val position = if (spread <= 0.0) 0.5 else ((current - low) / spread).coerceIn(0.0, 1.0)
    val trendPct = if (open == 0.0) 0.0 else ((current - open) / open) * 100.0
    val volatilityPct = if (average == 0.0) 0.0 else (spread / average) * 100.0
    return TimingHorizon(
        label = label,
        rangeLabel = "${formatRate(low)} - ${formatRate(high)}",
        positionLabel = "${(position * 100).toInt()}% of range",
        trendLabel = formatSignedPercent(trendPct),
        volatilityLabel = "${formatRate(volatilityPct)}%",
        position = position,
        trendPct = trendPct,
        volatilityPct = volatilityPct,
    )
}

private fun timingScore(horizon: TimingHorizon): Int {
    val positionScore = horizon.position * 72.0
    val trendScore = ((horizon.trendPct + 2.0) / 4.0).coerceIn(0.0, 1.0) * 20.0
    val volatilityPenalty = (horizon.volatilityPct / 12.0).coerceIn(0.0, 1.0) * 10.0
    return (positionScore + trendScore + 18.0 - volatilityPenalty).toInt().coerceIn(0, 100)
}
