package com.fxalways.app.screens

import com.fxalways.app.data.mock.DetailSeries
import com.fxalways.designsystem.components.Period

internal data class DetailStats(
    val open: Double,
    val high: Double,
    val low: Double,
    val average: Double,
    val volatilityPct: Double,
)

internal val Period.label: String
    get() = when (this) {
        Period.OneDay -> "1D"
        Period.OneWeek -> "1W"
        Period.OneMonth -> "1M"
        Period.OneYear -> "1Y"
        Period.All -> "ALL"
    }

internal fun List<Float>.seriesForPeriod(period: Period): List<Float> {
    val source = if (isEmpty()) DetailSeries else this
    val points = when (period) {
        Period.OneDay -> 6
        Period.OneWeek -> 8
        Period.OneMonth -> 18
        Period.OneYear -> source.size
        Period.All -> source.size
    }
    return source.takeLast(points.coerceAtMost(source.size)).ifEmpty { DetailSeries }
}

internal fun List<Float>.toDetailStats(): DetailStats {
    val source = if (isEmpty()) DetailSeries else this
    val values = source.map { it.toDouble() }
    val average = values.average().takeIf { !it.isNaN() } ?: 0.0
    val high = values.maxOrNull() ?: 0.0
    val low = values.minOrNull() ?: 0.0
    val volatility = if (average == 0.0) 0.0 else ((high - low) / average) * 100.0
    return DetailStats(
        open = values.firstOrNull() ?: 0.0,
        high = high,
        low = low,
        average = average,
        volatilityPct = volatility,
    )
}
