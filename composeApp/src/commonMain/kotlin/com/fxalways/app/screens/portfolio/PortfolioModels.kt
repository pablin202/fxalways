package com.fxalways.app.screens.portfolio

import com.fxalways.app.screens.*
import androidx.compose.runtime.Composable
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.formatRate

internal data class PortfolioHolding(
    val rate: FxRate,
    val amount: Double,
    val averageCostBase: Double,
) {
    val baseValue: Double = amountInBase(rate, amount)
    val dailyChangeInBase: Double = if (rate.rate == 0.0) 0.0 else baseValue * rate.change24h / 100.0
    val hasCostBasis: Boolean = averageCostBase > 0.0 && amount > 0.0
    val costBasisBase: Double = if (hasCostBasis) averageCostBase * amount else 0.0
    val unrealizedPnlBase: Double = if (hasCostBasis) baseValue - costBasisBase else 0.0
}

private fun amountInBase(rate: FxRate, amount: Double): Double =
    if (rate.rate == 0.0) 0.0 else amount / rate.rate

internal fun PortfolioHolding.weightLabel(portfolioValue: Double): String =
    if (portfolioValue <= 0.0 || baseValue <= 0.0) {
        "0%"
    } else {
        "${((baseValue / portfolioValue) * 100.0).toInt()}%"
    }

internal fun PortfolioHolding.dailyChangeLabel(baseCurrency: String): String {
    val sign = if (dailyChangeInBase >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(dailyChangeInBase))} today"
}

internal fun List<PortfolioHolding>.portfolioValueSeries(): List<Float> {
    if (isEmpty()) return emptyList()
    val pointCount = minOf(24, map { it.rate.sparkline.size }.filter { it > 0 }.minOrNull() ?: return emptyList())
    return List(pointCount) { index ->
        sumOf { holding ->
            val point = holding.rate.sparkline.getOrNull(index)?.toDouble() ?: holding.rate.rate
            if (point <= 0.0) 0.0 else holding.amount / point
        }.toFloat()
    }
}

internal fun List<Float>.changePercent(): Double =
    if (size < 2 || first() == 0f) 0.0 else (last() - first()) / first() * 100.0

internal fun formatSignedMoney(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}

internal fun formatPortfolioSignedPercent(change: Double): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign${formatRate(kotlin.math.abs(change))}%"
}

internal fun portfolioPnlPercentLabel(pnl: Double, costBasis: Double): String =
    if (costBasis <= 0.0) {
        "Add average cost"
    } else {
        val sign = if (pnl >= 0.0) "+" else "-"
        "$sign${formatRate(kotlin.math.abs(pnl / costBasis * 100.0))}%"
    }

internal fun allocationLabel(value: Double, portfolioValue: Double): String =
    if (portfolioValue <= 0.0 || value <= 0.0) "0%" else "${((value / portfolioValue) * 100.0).toInt()}%"

@Composable
internal fun portfolioActionPlan(
    largestHolding: PortfolioHolding?,
    largestDailyDriver: PortfolioHolding?,
    portfolioDailyChange: Double,
): String =
    when {
        largestHolding == null -> ui("Add amounts to activate portfolio guidance.")
        largestHolding.baseValue > 0.0 && largestDailyDriver?.rate?.code == largestHolding.rate.code && portfolioDailyChange < 0.0 ->
            "${ui("Review")} ${largestHolding.rate.code} ${ui("before adding more exposure")}"
        largestHolding.baseValue > 0.0 ->
            "${ui("Keep")} ${largestHolding.rate.code} ${ui("below concentration target")}"
        else -> ui("Review concentration before adding new exposure.")
    }

internal fun formatPortfolioChange(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}
