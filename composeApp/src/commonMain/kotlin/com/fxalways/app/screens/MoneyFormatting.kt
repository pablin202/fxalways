package com.fxalways.app.screens

import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.formatRate

internal fun convertedAmount(amount: Double, sourceRate: FxRate, targetRate: FxRate): Double =
    if (sourceRate.rate == 0.0) {
        0.0
    } else {
        amount / sourceRate.rate * targetRate.rate
    }

internal fun formatConvertedAmount(rate: FxRate, amount: Double): String =
    "${rate.code} ${if (rate.kind == CurrencyKind.Crypto) formatCryptoAmount(amount) else formatMoneyValue(amount)}"

internal fun formatCryptoAmount(value: Double): String =
    when {
        value <= 0.0 -> "0"
        value < 0.000001 -> "<0.000001"
        value < 1.0 -> formatRate(value)
        else -> formatMoneyValue(value)
    }

internal fun formatInputAmount(value: Double): String =
    when {
        value <= 0.0 -> ""
        value >= 100.0 -> formatMoneyValue(value).replace(",", "")
        value >= 1.0 -> formatRate(value)
        else -> formatRate(value)
    }

internal fun sanitizeAmountInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }.take(14)
    val decimalIndex = filtered.indexOfLast { it == '.' || it == ',' }
    if (decimalIndex < 0) return filtered
    val decimal = filtered[decimalIndex]
    val before = filtered.take(decimalIndex).filter { it.isDigit() }
    val after = filtered.drop(decimalIndex + 1).filter { it.isDigit() }
    return "$before$decimal$after"
}

internal fun parseAmountInput(value: String): Double {
    val normalized = if (value.count { it == ',' } == 1 && '.' !in value) {
        value.replace(',', '.')
    } else {
        value.replace(",", "")
    }
    return normalized.toDoubleOrNull() ?: 0.0
}

internal fun formatMoneyValue(value: Double): String =
    when {
        value == 0.0 -> "0.00"
        kotlin.math.abs(value) < 0.01 -> "<0.01"
        else -> formatRate(value)
    }

internal fun formatPercentValue(value: Double): String =
    ((value * 10.0).toInt() / 10.0).toString()

internal fun formatSignedPercent(value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return "$sign${formatPercentValue(kotlin.math.abs(value))}%"
}

internal fun formatSignedAmount(code: String, value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return "$sign$code ${formatRate(kotlin.math.abs(value))}"
}

internal data class PriceScannerHistoryEntry(
    val amountText: String,
    val targetCode: String,
    val sourceCode: String,
    val liveSourceCost: Double,
    val hiddenCost: Double,
)

internal fun liveSourceCostFor(scannedPrice: Double, targetRate: FxRate): Double =
    if (targetRate.rate > 0.0) scannedPrice / targetRate.rate else 0.0

internal fun hiddenCostFor(scannedPrice: Double, targetRate: FxRate, localMarketRate: Double): Double {
    val liveSourceCost = liveSourceCostFor(scannedPrice, targetRate)
    val localSourceCost = if (localMarketRate > 0.0) scannedPrice / localMarketRate else liveSourceCost
    return localSourceCost - liveSourceCost
}
