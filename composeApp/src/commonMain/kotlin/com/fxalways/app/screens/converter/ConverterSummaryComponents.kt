package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile

@Composable
internal fun ProviderSummaryCard(
    targetRate: FxRate,
    bestQuote: EstimatedFeeQuote?,
    customQuote: EstimatedFeeQuote?,
    midMarketValue: Double,
    potentialSavings: Double,
) {
    BentoCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Best provider"),
                    bestQuote?.provider?.let { ui(it) } ?: "--",
                    bestQuote?.let { "${ui("Recipient gets")} ${it.amount}" },
                    Modifier.weight(1f).testTag("converter_best_provider"),
                )
                MetricTile(
                    ui("Potential savings"),
                    formatConvertedAmount(targetRate, potentialSavings),
                    ui("vs worst visible provider"),
                    Modifier.weight(1f).testTag("converter_provider_savings"),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    ui("Mid-market value"),
                    formatConvertedAmount(targetRate, midMarketValue),
                    ui("before fees and markup"),
                    Modifier.weight(1f).testTag("converter_mid_market_value"),
                )
                MetricTile(
                    ui("Best loss"),
                    bestQuote?.loss ?: "${targetRate.code} 0.00",
                    bestQuote?.provider?.let { ui(it) },
                    Modifier.weight(1f).testTag("converter_best_loss"),
                )
            }
            bestQuote?.let {
                KeyValueRow(
                    ui("Best route"),
                    "${ui(it.provider)} · ${ui("Recipient gets")} ${it.amount}",
                    "${ui("Loss vs mid-market")} ${it.loss} (${it.lossPercent})",
                    modifier = Modifier.testTag("converter_best_route"),
                )
            }
            customQuote?.let {
                KeyValueRow(ui("Your custom cost"), it.loss, "${ui("Effective rate")} ${it.effectiveRate}")
            }
        }
    }
}

@Composable
internal fun CustomCostCard(
    sourceCode: String,
    fixedFeeText: String,
    feePercentText: String,
    markupPercentText: String,
    onFixedFeeChange: (String) -> Unit,
    onFeePercentChange: (String) -> Unit,
    onMarkupPercentChange: (String) -> Unit,
) {
    BentoCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(ui("CUSTOM COST"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeeInputField(
                    label = ui("Fixed fee"),
                    value = fixedFeeText,
                    suffix = sourceCode,
                    modifier = Modifier.weight(1f),
                    onValueChange = onFixedFeeChange,
                )
                FeeInputField(
                    label = ui("Fee %"),
                    value = feePercentText,
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    onValueChange = onFeePercentChange,
                )
                FeeInputField(
                    label = ui("FX markup"),
                    value = markupPercentText,
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    onValueChange = onMarkupPercentChange,
                )
            }
        }
    }
}

@Composable
internal fun FeeQuotesListCard(quotes: List<EstimatedFeeQuote>) {
    BentoCard(padding = 0.dp) {
        Column {
            quotes.forEachIndexed { index, quote ->
                FeeComparisonRow(quote, rank = index + 1)
            }
        }
    }
}
